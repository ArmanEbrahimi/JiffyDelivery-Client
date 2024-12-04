package com.example.jiffydeliveryclient.remote

import android.util.Log
import com.google.auth.oauth2.GoogleCredentials
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.nio.charset.StandardCharsets

class GetAccessToken {
    val firebaseMessagingScope = "https://www.googleapis.com/auth/firebase.messaging"
    fun getAccessToke() : String?{
        try {
            val jsonString = "{\n" +
                    "  \"type\": \"service_account\",\n" +
                    "  \"project_id\": \"jiffy-delivery-80151\",\n" +
                    "  \"private_key_id\": \"ecd86d2e67dffcab6ae8a2bc3e5bd50ca8d4f497\",\n" +
                    "  \"private_key\": \"-----BEGIN PRIVATE KEY-----\\nMIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCvXtjN0gD76n5+\\nuXuo+bBcoJb1RWm47Gjt6SRJa040kNPBVY+WJZQOEnc2uLO87lQ0gpHJzSks0RF4\\ncoADLBTFsEjfCc7G0B1qR+9SOWVmPPVBybm+3MiWN10lkxeG9VPhl1E9RnV9rYxz\\nobQVGRqVelk2np3JU86d1k+e+uaJWvYyT5omEfoy4Nh/iZTxWkcwYkJLySv7ufsM\\nPm+O0rabHm7LcZfvRdHbOQttDpD6VU95ntcsmQnNcFUbuslUjAtb9Dda9X+U2Bib\\n/ocTkQ90WSrwQMVFAPPY0dlrM7swur4uvqdXbfnAH3FbNiBLZQsWiGlWPYQE8P7W\\ne1z0RzolAgMBAAECggEAF7pWTMDXi74+V8Cac/I2s82xpyV5aMEp8Fi9ZWasqe2X\\njR7Zwue6OM40TZXfkdZQU9t/xMVqxwrRmZGqcuNtuxK/RVhDH0DM3hK2FZK4/sSM\\nLZbQ+DtQs4xxoLIeeo7qhsktv4PvRS4sZP/RyRIAuA1aEUsUT8d0WwRCeSD6/MX1\\nN4P7mP0h6Fxarm/p2FytmVdt6R72YGgTdlE9MoIKpcLrFk0puO9C7df0wFugJ+cO\\nUJiPxWd0236Rm2XCEY/SbVIvyZzyx8shg4XVooOV3svQ6EropmDeZfbem0q5vVKt\\nQ8qRFhn8pp9d0kqdmtLZ2+D0G7ay2iVStrh7i9HBqQKBgQD1mqGZCqX9QQvGGgND\\nRXIDDZRVHIJXRxkBe6MVq2Pu7TP5llFV5s+qTjF4bkdKfLU4JK6dL/Hyu+xkb9IR\\nzBEQHYhPeezaOgkUMDrQ/p8mD0TgQyOqTSDAlijLn7U7a6tzZ/M/aA3ipzY+46bA\\nFUbxKq36Z7TYS4SMysWvysoLLQKBgQC2yyoBkeJnCwr+PJkrnqe4dgH6xEXMgMPE\\ncH10nXr5I8xsNUU2EhrMZaztDeGRjXnrGR9rOA3Pa8fL+nY2DhwSGQYkYesX0YLp\\n3suYc2qmirpa2tl6dn46aR108Zm8v+bOY82gFTjgiVpbFxWpZjlVyM9l5dPtL1iw\\nFjLHL+dl2QKBgQC74GbfoJKrcZelGf8G9deqWDfxB5b5VszdIov1pp8ShWdt9LwR\\nXzfaSmhzfMCSTWV/Jihg2+1URsclaZijvpwK3NyKF3XE1gJd7CsB9tUdky6X8yQU\\nQV3dlMBuS6SiaNHFvW4JQfrlOA45+4FyjJQxJfnihHSFxPcHkJG60QDPnQKBgB1Y\\nRjkUS21RZ4VjAw/um2xQfYbdZXssHMF3cZx7/5bpeVLRxnektmqj0/wGY3TzamQk\\nl1emGwl2ew+y+LE62KTLIaJLmesr9kFHRBYISVHNKZeWVJ2FUkRcRZ9VfeHASPRP\\nL16IqaXaRwgij5avU3sDDtCD8d684Bb8qVAyz4oZAoGBAOzlIflSltN3LbeAnoR7\\nHPfhejJotvVOFrYJ8VQhZGCzK6MZlREsgeRv4eHvdmk8n4h103aOBH4HR2OujVD4\\nNWbBvAxCHMsa4ZuGjXAvzWJ9Uy2U73ooXkmM+jalbyQXbhnjPY4Qh6hJKvJ9luo9\\n+IIa+jrJ70fb234B1RuoAfcx\\n-----END PRIVATE KEY-----\\n\",\n" +
                    "  \"client_email\": \"firebase-adminsdk-twhmh@jiffy-delivery-80151.iam.gserviceaccount.com\",\n" +
                    "  \"client_id\": \"105034695641157815958\",\n" +
                    "  \"auth_uri\": \"https://accounts.google.com/o/oauth2/auth\",\n" +
                    "  \"token_uri\": \"https://oauth2.googleapis.com/token\",\n" +
                    "  \"auth_provider_x509_cert_url\": \"https://www.googleapis.com/oauth2/v1/certs\",\n" +
                    "  \"client_x509_cert_url\": \"https://www.googleapis.com/robot/v1/metadata/x509/firebase-adminsdk-twhmh%40jiffy-delivery-80151.iam.gserviceaccount.com\",\n" +
                    "  \"universe_domain\": \"googleapis.com\"\n" +
                    "}"
            val stream:InputStream = ByteArrayInputStream(jsonString.toByteArray(StandardCharsets.UTF_8))
            val googleCredentials : GoogleCredentials = GoogleCredentials.fromStream(stream)
                .createScoped(arrayListOf(firebaseMessagingScope))
            googleCredentials.refresh()
            return googleCredentials.accessToken.tokenValue
        }catch (e:IOException){
            Log.d("error",e.message!!)
            return null
        }
    }
}