/*
 * Copyright (C) 2020 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.businessmessages.samples.servlets;

// [START callback for receiving consumer messages]

// [START import_libraries]

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.businessmessages.v1.Businessmessages;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesCardContent;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesCarouselCard;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesContentInfo;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesDialAction;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesEvent;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesMedia;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesMessage;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesOpenUrlAction;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesRepresentative;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesRichCard;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesStandaloneCard;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesSuggestedAction;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesSuggestedReply;
import com.google.api.services.businessmessages.v1.model.BusinessMessagesSuggestion;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.communications.businessmessages.v1.CardWidth;
import com.google.communications.businessmessages.v1.EventType;
import com.google.communications.businessmessages.v1.MediaHeight;
import com.google.communications.businessmessages.v1.RepresentativeType;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
// [END import_libraries]

/**
 * Servlet for receiving consumer messages and sending a response.
 *
 * Once the TODOs are complete, the following commands are supported:
 *
 * - card - Sends a sample rich card
 * - carousel - Sends a sample carousel
 * - chips - Sends a message with suggested replies
 *
 * Any other incoming message is echoed back to the end-user.
 */
@WebServlet(name = "AgentCallback", value = "/callback")
public class AgentCallback extends HttpServlet {

  private static final Logger logger = Logger.getLogger(AgentCallback.class.getName());

  private static final String EXCEPTION_WAS_THROWN = "exception";

  private static final String CREDENTIALS_FILE_NAME = "bm-agent-service-account-credentials.json";

  // Set of commands the bot understands
  private static final String CMD_RICH_CARD = "card";
  private static final String CMD_CAROUSEL_CARD = "carousel";
  private static final String CMD_SUGGESTIONS = "chips";

  // Images used in cards and carousel examples
  private static final String SAMPLE_IMAGES[] = new String[]{
      "https://storage.googleapis.com/kitchen-sink-sample-images/cute-dog.jpg",
      "https://storage.googleapis.com/kitchen-sink-sample-images/elephant.jpg",
      "https://storage.googleapis.com/kitchen-sink-sample-images/adventure-cliff.jpg",
      "https://storage.googleapis.com/kitchen-sink-sample-images/sheep.jpg",
      "https://storage.googleapis.com/kitchen-sink-sample-images/golden-gate-bridge.jpg"};

  // Object to maintain OAuth2 credentials to call the BM API
  private GoogleCredential credential;

  // Reference to the BM api builder
  private Businessmessages.Builder builder;

  public AgentCallback() {
    super();

    initBmApi();
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    // set the response type to JSON
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");

    String jsonResponse = request.getReader().lines().collect(
        Collectors.joining(System.lineSeparator())
    );

    logger.info(jsonResponse);

    // load the JSON string into a Json parser
    JsonParser parser = new JsonParser();
    JsonObject obj = parser.parse(jsonResponse).getAsJsonObject();

    String conversationId = obj.get("conversationId").getAsString();

    // Use memcache to de-dupe messages
    MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();

    // Check that the object has a requestId
    if (obj.has("requestId")) {
      String requestId = obj.get("requestId").getAsString();

      // Check to see if this response has already been seen, if so, ignore
      if (!syncCache.contains(requestId)) {
        syncCache.put(requestId, true);

        // Check that the message object contains a text value, otherwise ignore
        if (obj.has("message")) {
          obj = obj.get("message").getAsJsonObject();
          if (obj.has("text")) {
            String message = obj.get("text").getAsString();

            routeMessage(message, conversationId);
          }
        } else if (obj.has("suggestionResponse")) {
          obj = obj.get("suggestionResponse").getAsJsonObject();
          if (obj.has("text")) {
            String message = obj.get("text").getAsString();

            routeMessage(message, conversationId);
          }
        } else if (obj.has("userStatus")) {
          obj = obj.get("userStatus").getAsJsonObject();

          if (obj.has("isTyping")) {
            logger.info("User is typing");
          } else if (obj.has("requestedLiveAgent")) {
            logger.info("User requested transfer to live agent");
          }
        }
      }
    }
  }

  /**
   * Routes the message received from the user to create a response.
   *
   * @param message The message text received from the user.
   * @param conversationId The unique id for this user and agent.
   */
  private void routeMessage(String message, String conversationId) {
    String normalizedMessage = message.trim().toLowerCase();

    // TODO: Update the routing to call the appropriate function
    // based on matching the normalizedMessage value to the
    // supported commands for chips, cards, and carousels.

    echoMessage(message, conversationId);
  }

  /**
   * Sends a sample rich card to the user.
   *
   * @param conversationId The conversation ID that uniquely maps to the user and agent.
   */
  private void sendRichCard(String conversationId) {
    // TODO: Create a BusinessMessagesStandaloneCard card and
    // use the sendResponse function to send a rich card with
    // a title, description, image, a suggested reply, an action
    // to open the phone's dialer, and an action to open a URL.
  }

  /**
   * Sends a sample carousel rich card to the user.
   *
   * @param conversationId The conversation ID that uniquely maps to the user and agent.
   */
  private void sendCarousel(String conversationId) {
    // TODO: Create a BusinessMessagesCarouselCard card and
    // use the sendResponse function to send a carousel
    // using the SAMPLE_IMAGES as the media element. Each card should
    // have a title, description, image, a suggested reply, an action
    // to open the phone's dialer, and an action to open a URL.
  }

  /**
   * Sends a message with a suggested replies.
   *
   * @param conversationId The conversation ID that uniquely maps to the user and agent.
   */
  private void sendMessageWithSuggestions(String conversationId) {
    // TODO: Use the sendResponse function to send a text message
    // with chips for a suggested reply, an action
    // to open the phone's dialer, and an action to open a URL.
  }

  /**
   * Sends the message received from the user back to the user.
   *
   * @param message The message text received from the user.
   * @param conversationId The unique id for this user and agent.
   */
  private void echoMessage(String message, String conversationId) {
    sendResponse(new BusinessMessagesMessage()
        .setMessageId(UUID.randomUUID().toString())
        .setText(message)
        .setRepresentative(getRepresentative()), conversationId);
  }

  private BusinessMessagesRepresentative getRepresentative() {
    return new BusinessMessagesRepresentative()
        .setRepresentativeType(RepresentativeType.BOT.toString())
        .setDisplayName("Echo Bot")
        .setAvatarImage("https://storage.googleapis.com/sample-avatars-for-bm/bot-avatar.jpg");
  }

  /**
   * Posts a message to the Business Messages API, first sending a typing indicator event and
   * sending a stop typing event after the message has been sent.
   *
   * @param message The message object to send the user.
   * @param conversationId The conversation ID that uniquely maps to the user and agent.
   */
  private void sendResponse(BusinessMessagesMessage message, String conversationId) {
    try {
      // Send typing indicator
      BusinessMessagesEvent event =
          new BusinessMessagesEvent()
              .setEventType(EventType.TYPING_STARTED.toString());

      Businessmessages.Conversations.Events.Create request
          = builder.build().conversations().events()
          .create("conversations/" + conversationId, event);

      request.setEventId(UUID.randomUUID().toString());
      request.execute();

      logger.info("message id: " + message.getMessageId());
      logger.info("message body: " + message.toPrettyString());

      // Send the message
      Businessmessages.Conversations.Messages.Create messageRequest
          = builder.build().conversations().messages()
          .create("conversations/" + conversationId, message);

      messageRequest.execute();

      // Stop typing indicator
      event =
          new BusinessMessagesEvent()
              .setEventType(EventType.TYPING_STOPPED.toString());

      request
          = builder.build().conversations().events()
          .create("conversations/" + conversationId, event);

      request.setEventId(UUID.randomUUID().toString());
      request.execute();
    } catch (Exception e) {
      logger.log(Level.SEVERE, EXCEPTION_WAS_THROWN, e);
    }
  }

  /**
   * Creates a list of sample suggestions that includes a suggested reply and two actions.
   *
   * @return A list of sample suggestions.
   */
  private List<BusinessMessagesSuggestion> getSampleSuggestions() {
    return new ArrayList<BusinessMessagesSuggestion>() {{
      add(new BusinessMessagesSuggestion()
          .setReply(new BusinessMessagesSuggestedReply()
              .setText("Sample Chip").setPostbackData("sample_chip")
          ));
      add(new BusinessMessagesSuggestion()
          .setAction(new BusinessMessagesSuggestedAction()
              .setText("URL Action").setPostbackData("url_action")
              .setOpenUrlAction(
                  new BusinessMessagesOpenUrlAction().setUrl("https://www.google.com"))
          ));
      add(new BusinessMessagesSuggestion()
          .setAction(new BusinessMessagesSuggestedAction()
              .setText("Dial Action").setPostbackData("dial_action")
              .setDialAction(new BusinessMessagesDialAction().setPhoneNumber("+12223334444"))
          ));
    }};
  }

  /**
   * Initializes credentials used by the Business Messages API.
   */
  private void initCredentials() {
    logger.info("Initializing credentials for Business Messages.");

    try {
      this.credential = GoogleCredential.getApplicationDefault();
      this.credential = credential.createScoped(Arrays.asList(
          "https://www.googleapis.com/auth/businessmessages"));

      this.credential.refreshToken();
    } catch (Exception e) {
      logger.log(Level.SEVERE, EXCEPTION_WAS_THROWN, e);
    }
  }

  /**
   * Initializes the BM API object.
   */
  private void initBmApi() {
    logger.info("Initializing Business Messages API");

    if (this.credential == null) {
      initCredentials();
    }

    try {
      HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
      JacksonFactory jsonFactory = JacksonFactory.getDefaultInstance();

      // create instance of the BM API
      builder = new Businessmessages
          .Builder(httpTransport, jsonFactory, null)
          .setApplicationName("Echo Bot");

      // set the API credentials and endpoint
      builder.setHttpRequestInitializer(credential);
    } catch (Exception e) {
      logger.log(Level.SEVERE, EXCEPTION_WAS_THROWN, e);
    }
  }
}
// [END callback for receiving consumer messages]
