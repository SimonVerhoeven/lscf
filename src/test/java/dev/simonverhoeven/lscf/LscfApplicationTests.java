package dev.simonverhoeven.lscf;

import org.apache.maven.shared.invoker.DefaultInvocationRequest;
import org.apache.maven.shared.invoker.DefaultInvoker;
import org.apache.maven.shared.invoker.MavenInvocationException;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.localstack.LocalStackContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class LscfApplicationTests {

	@Test
	void runFunctionLocally() throws Exception {
		File rootFolder = getRootFolder();
		buildProject(rootFolder);

		try (LocalStackContainer localStack = new LocalStackContainer(DockerImageName.parse("localstack/localstack:latest"))
				.withCopyFileToContainer(MountableFile.forHostPath(Paths.get(rootFolder.getCanonicalPath(), "target\\lscf-0.0.1-SNAPSHOT-aws.jar")), "/opt/code/localstack/lambda.jar")
				.withServices(LocalStackContainer.Service.LAMBDA)
				.withExposedPorts(8080)) {
			localStack.start();

			localStack.execInContainer("awslocal", "lambda", "create-function",
					"--function-name", "lambda-name",
					"--runtime", "java17",
					"--handler", "org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest",
					"--role", "arn:aws:iam::123456789101:role/lcsf",
					"--zip-file", "fileb://lambda.jar");

			localStack.execInContainer("awslocal", "lambda", "wait", "function-active-v2", "--function-name","lambda-name");

			final var createFunctionResult = localStack.execInContainer("awslocal", "lambda", "create-function-url-config", "--function-name","lambda-name","--auth-type","NONE");
			final var functionUrl = new JSONObject(createFunctionResult.getStdout()).get("FunctionUrl").toString();

			var result = localStack.execInContainer("curl", functionUrl, "-H", "'Content-type: application/json'");
			System.out.println(result);

			// Delete the function, otherwise the spawned container will keep running.
			localStack.execInContainer("awslocal", "lambda", "delete-function", "--function-name", "lambda-name");
		}
	}

	private static void buildProject(File rootFolder) throws MavenInvocationException {
		var request = new DefaultInvocationRequest()
				.setPomFile(new File(rootFolder,"pom.xml"))
				.setGoals(List.of("clean", "package","-DskipTests"))
				.setMavenExecutable(new File(rootFolder,"mvnw"));

		new DefaultInvoker().execute(request);
	}

	private static File getRootFolder() {
		var cwd = new File(".");
		while (!new File(cwd, "mvnw").isFile()) {
			cwd = cwd.getParentFile();
		}
		return cwd;
	}
}
