# Authentication Micro Service

A simple REST service that takes care of authentication and user management tasks.

Acts as learning project for Scala, Spray and generally FP.

Working:
* add account to in memory storage
* authenticate account against local storage
* authenticate via social login (oauth2)

Open features until working version:
* configure google provider via rest api (clientId, secret, scopes)
* consistence via mongodb
* provide docker file
* sample app to show usage of api
* password reset

Other open features:
* api call get access token from provider
* support different oauth2 providers (facebook, linkedin, github)
* mirror active directory accounts
