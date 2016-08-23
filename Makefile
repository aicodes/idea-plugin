upload:
	cp aicodes-idea-plugin.jar ~/Aicodes/website
	cd ~/Aicodes/website
	gsutil cp aicodes-idea-plugin.jar gs://www.ai.codes
	gsutil acl ch -u AllUsers:R gs://www.ai.codes/aicodes-idea-plugin.jar
