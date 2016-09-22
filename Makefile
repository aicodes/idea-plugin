upload:
	gsutil cp  build/distributions/aicodes-idea-plugin.zip gs://www.ai.codes
	gsutil acl ch -u AllUsers:R gs://www.ai.codes/aicodes-idea-plugin.zip
