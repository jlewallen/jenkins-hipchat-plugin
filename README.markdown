## HipChat plugin for Jenkins

A Jenkins plugin that can send notifications to HipChat chat rooms for build events.

### Features

* Supports both v1 and v2 API
* Ability to test notifications
* Notify Build Start
* Notify Aborted
* Notify Failure
* Notify Not Built
* Notify Success
* Notify Unstable
* Notify Back To Normal

### Proxy settings

Proxy support was added with version 0.1.8 - it uses the HTTP proxy configuration from the Jenkins plugin manager advanced settings.

### Configuration

When using v1 API, an API token needs to be provided, otherwise an OAuth2 access token with send_notification scope shall be used.
