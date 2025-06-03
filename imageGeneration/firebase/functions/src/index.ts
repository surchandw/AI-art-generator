/**
 * Import function triggers from their respective submodules:
 *
 * import {onCall} from "firebase-functions/v2/https";
 * import {onDocumentWritten} from "firebase-functions/v2/firestore";
 *
 * See a full list of supported triggers at https://firebase.google.com/docs/functions
 */
// Start writing functions
// https://firebase.google.com/docs/functions/typescript

// import {onRequest} from "firebase-functions/v2/https";
import * as logger from "firebase-functions/logger";

import * as functions from "firebase-functions";

import {SecretManagerServiceClient} from "@google-cloud/secret-manager";
const client = new SecretManagerServiceClient();

const projectId = "REPLACE_WITH_PROJECT_ID";

const secretId = "REPLACE_WITH_API_KEY";

async function getSecretValue(name: string) {
  const [version] = await client.accessSecretVersion({
    name: `projects/${projectId}/secrets/${name}/versions/latest`,
  });
  const payload = version.payload?.data?.toString();
  return payload;
}

export const getSTAronbaSho = functions.https.onRequest(
  async (request, response) => {
    try {
      const mySecret = await getSecretValue(secretId);
      const successString = `{"status": "success", "data": "${mySecret}"}`;
      const successResponse: JSON = JSON.parse(successString);
      response.status(200).send(successResponse);
      logger.debug("sent data");
    } catch (e) {
      response.status(500).json({
        "status": "fail",
        "data": null,
      }).send();
      logger.debug("Exception occured");
    }
  }
);
