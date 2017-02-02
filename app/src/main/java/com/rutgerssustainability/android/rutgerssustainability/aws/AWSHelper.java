package com.rutgerssustainability.android.rutgerssustainability.aws;

import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.rutgerssustainability.android.rutgerssustainability.utils.Constants;

/**
 * Created by shreyashirday on 1/19/17.
 */
public class AWSHelper {

    private static TransferUtility transferUtility;
    private static AmazonS3 amazonS3;
    private static CognitoCachingCredentialsProvider credentialsProvider;

    private static CognitoCachingCredentialsProvider getCredentialsProvider(Context context) {
        if (credentialsProvider == null) {
            // Initialize the Amazon Cognito credentials provider
            credentialsProvider = new CognitoCachingCredentialsProvider(
                    context,
                    "us-east-1:36554b1f-7640-4a2f-ab12-d8567abbe228", // Identity Pool ID
                    Regions.US_EAST_1 // Region
            );
        }
        return credentialsProvider;
    }

    private static AmazonS3 getAmazonS3(Context context) {
        if (amazonS3 == null) {
            amazonS3 = new AmazonS3Client(getCredentialsProvider(context));
        }
        return amazonS3;
    }

    public static TransferUtility getTransferUtility(Context context) {
        if (transferUtility == null) {
            transferUtility = new TransferUtility(getAmazonS3(context), context);
        }
        return transferUtility;
    }

    public static String createS3FileUrl(String filename) {
        return Constants.AWS.BUCKET_URL + "/" + filename;
    }

}
