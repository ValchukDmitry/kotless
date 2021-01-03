# Kotless roadmap

## 0.1.*

* Documentation and accessibility
    * Add chatbot creation examples
    * Rework a bit documentation to make it more clear
    * Add some introduction on serverless development
* Support of Google Cloud Platform
* Support of GraalVM

## 0.2.*

* IDEA plugin
    * Inspections on permissions granting - detect usage of AWS SDK functions that are not permitted
      explicitly
    * Deployed functions logs - possibility to attach IDEA console to CloudWatch log of specific
      lambda

## To discuss

* Support of Azure Cloud
* Extension for Cognito authentication
* Event handlers - functions as handlers for different AWS events
* DSL libraries for S3, DynamoDB, SSM at least
* Implicit permissions flow - possibility to deduce permissions from AWS SDK functions only.

## Later plans

* Async calls of other lambdas via `async { ... }`. Other lambda will be generated from body
  of `async` function.
* Async batch calls of other lambdas via `asyncBatching(list, max = N) { ... }`. Will use FireHose
  to batch passed elements from different calls of lambda into N elements packs and pass them to
  body for batch execution.
* Implementation of Kotlin/Native and Kotlin/JS dispatchers
