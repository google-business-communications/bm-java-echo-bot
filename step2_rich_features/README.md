# BUSINESS MESSAGES: Echo Bot

This sample demonstrates how to receive a message from the [Business Messages](https://developers.google.com/business-communications/business-messages/reference/rest)
platform and echo the same message back to the user using the
[Business Messages Java client library](https://github.com/google-business-communications/java-businessmessages).

In this step, there are defined TODOs within the [AgentCallback.java](https://github.com/google-business-communications/bm-java-echo-bot/blob/master/step2_rich_features/src/main/java/com/google/businessmessages/samples/servlets/AgentCallback.java) file when
completed will add functionality for the following commands:
* `card` - The bot responds with a sample rich card
* `carousel` - The bot responds with a sample carousel
* `chips` - The bot responds with sample suggested replies

This sample runs on the Google App Engine.

See the Google App Engine (https://cloud.google.com/appengine/docs/java/) standard environment
documentation for more detailed instructions.

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
1. Open the [Create an agent](https://developers.google.com/business-communications/business-messages/guides/set-up/agent)
guide and follow the instructions to create a Business Messages agent.

## Deploy the sample

1.  In a terminal, navigate to this sample's root directory.

1.  Run the following commands:

    ```bash
    gcloud config set project PROJECT_ID
    ```

    Where PROJECT_ID is the project ID for the project you created when you registered for
    Business Messages.

    ```base
    mvn appengine:deploy
    ```

1.  On your mobile device, use the test business URL associated with the
    Business Messages agent you created. Open a conversation with your agent
    and type in "Hello". Once delivered, you should receive "Hello" back
    from the agent.

    Once the TODOs are completed, try entering "card", "carousel", and "chips"
    separately to explore other functionality.

    See the [Test an agent](https://developers.google.com/business-communications/business-messages/guides/set-up/agent#test-agent) guide if you need help retrieving your test business URL.