# Welcome to Cloud Functions for Firebase for Python!
# To get started, simply uncomment the below code or create your own.
# Deploy with `firebase deploy`

import os
from firebase_functions import https_fn
from google.cloud import secretmanager

@https_fn.on_request(secrets=["SECRET_NAME"])
def get_secret_key(req):
    #myBillingService = initialize_billing(key=os.environ.get('SECRET_NAME'))


    # GCP project in which to store secrets in Secret Manager.
    project_id = "REPLACE_WITH_PROJECT_NAME"

    # ID of the secret to create.
    secret_id = "REPLACE_WITH_API_KEY"

    # Version
    version_no = "1"

    # Create the Secret Manager client.
    client = secretmanager.SecretManagerServiceClient()

    # Build the parent name from the project.
    secret_name = f"projects/{project_id}/secrets/{secret_id}/versions/{version_no}"

    # Access the secret version.
    response = client.access_secret_version(request={"name": secret_name})


    secret_key = response.payload.data.decode("UTF-8")
    print(f"Plaintext: {secret_key}")

    return secret_key













'''
s V2
#import * as functions from "firebase-functions/v2"
# 2. import the defineSecret function
import defineSecret from "firebase-functions/params"

# 3. define the secrets, using the keys that we previously stored with the Firebase CLI
# https://firebase.google.com/docs/functions/2nd-gen-upgrade#special_case_api_keys
const stabilityAiApiKey = defineSecret("API_KEY")

# this function needs to access the stabilityAiApiKey only
exports.createOrderPaymentIntent = https_fn.https.onCall({ secrets: [stabilityAiApiKey] }, (context) => {
  return createOrderPaymentIntent(context)
})


initialize_app()


@https_fn.on_request()
def get_key(req: https_fn.Request) -> https_fn.Response:
  #get the first secret key value
  const secretKey = stabilityAiApiKey.value()
  # if it's empty, throw an error
  if (secretKey.length === 0) {
    console.error("API Key is not set")
    res.sendStatus(400)
  }
  return https_fn.Response(secretKey)

  '''
