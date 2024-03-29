# LSCF

***

## About 

This project is a sample implementation of a spring cloud function that will return you a randomly generated number.
You might not want certain applications to be continuously running for a simple call, so an event-driven serverless function (aka it gets started by a certain trigger) can save you quite a bit in the long rung.
The advantage of spring cloud function is that we can easily implement business logic through functions, without having to care about the underlying platform (AWS Lambda, Apache OpenWhisk, Google cloud functions, microsoft azure, ... ) beyond having to switch adapters.

AWS recently announced support for Java 17, which means we can now write functions using spring boot 3 (which uses java 17 as the baseline) and spring cloud functions so it seemed like a nice opportunity to set something up. 
See for reference: https://aws.amazon.com/blogs/compute/java-17-runtime-now-available-on-aws-lambda/

The function itself makes use of functional definitions, rather than traditional bean definitions to speed up the startup.

The main differences are:
- the main class is an `ApplicationContextInitializer` annotated with `@SpringBootConfiguration` rather than `@SpringBootApplication`
- generateRandomNumber isn't annotated with `@Bean`, but registered in the initialize method
- `FunctionalSpringApplication` is used rather than its parent class `SpringApplication` 

**Notes**:
- using `@SpringBootConfiguration` indicates we're not enabling spring boot autoconfiguration
- alternatively we could also have created a class of type `Function` and registered it with `SpringApplication`

This project makes use of a [localstack](https://localstack.cloud/) [testcontainer](https://www.testcontainers.org/) so that we can run the function locally. (LscfApplicationTests).

This is just one of the ways to test/run your `Function` locally.

Other means to test this:
* [AWS Lambda Runtime Emulator (RIE)](https://docs.aws.amazon.com/lambda/latest/dg/images-test.html) which acts as a proxy for the Lambda Runtime API and is part of the AWS Lambda base image.
* `spring-boot-starter-web(flux)` so you can call it locally
* `FunctionalSpringBootTest` with a call to your function
* ...

But it's a nice way to see one of the possibilities of the LocalStack testcontainer.

***

## Execution

You do not need to have maven installed locally, you can just run `./mvnw test`.  
**Note** this can take a bit as the function is compiled as part of the test, and the testcontainers need to start.

*** 

## Requirements

JDK 17, if you do not have it installed yet, and if you have [sdkman](https://sdkman.io/) you can run `sdk env install`

***

## Pitfalls

Just like any other solution cloud functions do have their drawbacks or no added value: 
* they can add extra complexity
* you need a proper monitoring setup
* they can be challenging at times to debug/troubleshoot
* if they are just for orchestration `aws step functions`/`gcp workflows`/... might be a better solution

So give some consideration to your usecase, and do not be afraid to reevaluate it every now and then.

***

## Extra

If you want to deploy this to AWS the request handler is: `org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest`  
To speed up subsequent invocations of this function on AWS you can go to `settings => basic settings => edit` and set `SnapStart` to `PublishedVersions` and then create a new version and publish it.
