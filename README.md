# BUSINESS MESSAGES: Echo Bot

This sample demonstrates how to use the [Business Messages Java client library](https://github.com/google-business-communications/java-businessmessages) for performing operations
with the [Business Messages API](https://developers.google.com/business-communications/business-messages/reference/rest).

This sample contains multiple example codebases. Each subfolder
is a complete example bot and can be deployed to Google App Engine
to support a Business Messages conversational experience.

This application assumes that you're signed up with
[Business Messages](https://developers.google.com/business-communications/business-messages/guides/set-up/register).

## Documentation

The documentation for the Business Messages API can be found [here](https://developers.google.com/business-communications/business-messages/reference/rest).

## Prerequisite

You must have the following software installed on your machine:

* [Apache Maven](http://maven.apache.org) 3.3.9 or greater
* [Google Cloud SDK](https://cloud.google.com/sdk/) (aka gcloud)
* [Java 8](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [Google App Engine SDK for Java](https://cloud.google.com/appengine/docs/standard/java/download)

## Before you begin

1.  [Register with Business Messages](https://developers.google.com/business-communications/business-messages/guides/set-up/register).
1.  Once registered, follow the instructions to [enable the APIs for your project](https://developers.google.com/business-communications/business-messages/guides/set-up/register#enable-api).

## Samples

Each sample has a `README.md` with instructions for running the sample.

| Sample                      | Description                       |
| --------------------------- | --------------------------------- |
| [step1_base](https://github.com/google-business-communications/bm-java-echo-bot/tree/master/step1_base) | Base code for supporting an echo bot on Business Messages. |
| [step2_rich_features](https://github.com/google-business-communications/bm-java-echo-bot/tree/master/step2_rich_features) | Extension of the base code that contains TODOs to create a rich messaging experience. |
| [full_sample](https://github.com/google-business-communications/bm-java-echo-bot/tree/master/full_sample) | Complete solution for adding rich features. |
| [message_validation_sample](https://github.com/google-business-communications/bm-java-echo-bot/tree/master/message_validation_sample) | Demonstrates how to validate messages com from Google. |

## Learn more

To learn more about setting up Business Messages and supporting
chat from Search and Maps, see the [documentation](https://developers.google.com/business-communications/business-messages/guides).