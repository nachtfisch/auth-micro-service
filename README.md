# Authentication Micro Service

A simple REST service that takes care of authentication and user management tasks.

Acts as learning project for Scala, Spray and generally FP.

Working:
* add account to in memory storage
* authenticate account against local storage
* authenticate via social login (oauth2)


Open features:
* get access token from provider
* support different oauth2 providers (facebook, linkedin, github)
* password reset
* mirror active directory accounts
