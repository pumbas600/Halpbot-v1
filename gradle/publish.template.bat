cmd /k ^
.\..\gradlew publishMavenJavaPublicationToOssrhRepository ^
-Psigning.secretKeyRingFile=C:/WhereverYouStoredIt/YourSecretKey.gpg ^
-Psigning.password=ThePasswordForTheKey ^
-Psigning.keyId=TheKeyID ^
-ossrh.username=YourJiraID ^
-ossrh.password=YourJiraPassword

@pause