package io.lippia.reporting;

public interface Notifier {
    /**
     * Sends a message to the configured notification service.
     *
     * @param message The message to send.
     * @throws Exception If an error occurs while sending the message.
     */
    void sendMessage(String message) throws Exception;

    /**
     * Returns the webhook URL for the notification service.
     *
     * @return The webhook URL.
     */
    String getWebhookUrl();
}