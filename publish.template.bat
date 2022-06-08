gradle publishMavenJavaPublicationToOssrhRepository ^
-Psigning.secretKeyRingFile=C:/WhereverYouStoredIt/YourSecretKey.gpg ^
-Psigning.password=ThePasswordForTheKey ^
-Psigning.keyId=TheKeyID ^
-Possrh.username=YourJiraID ^
-Possrh.password=YourJiraPassword

@pause