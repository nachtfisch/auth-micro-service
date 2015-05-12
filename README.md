# Authentication Micro Service

A simple REST service that takes care of authentication and user management tasks. The idea is to have sort of a micro service to not implement authentication and authorization again and again. 

Since it acts as a learning project to get into Scala, Spray.io and Scala FP there is a lot of 'try out' code here and also to see how different REST API requirements can be solved by the means of spray.

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
