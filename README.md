# Authentication Micro Service

A simple micro service providing a REST API to deal with authentication.

Planned features:
* create accounts
* password reset
* mirror active directory accounts
* authenticate account
* authenticate via social login (oauth2)


HttpResponse(200 OK,HttpEntity(application/json; charset=UTF-8,{
 "access_token": "ya29.UgGojk-DTp4VKBTOA0tEktZ3_6QkUINCf7MhpzNesoGfmFwSdPE5ajkUgxQcR7qkBJzVwuSSCfraRw",
 "token_type": "Bearer",
 "expires_in": 3600,
 "id_token": "eyJhbGciOiJSUzI1NiIsImtpZCI6IjM0ZmE1NTkxZmU1OTQ2YTZkYjU1NTVhZjczNzdiODEzOGQxMTVkYjYifQ.eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwic3ViIjoiMTA1MTk4MzUwNzQ5MTY5MTg5NzMzIiwiYXpwIjoiODIxNTEyMzU3NDguYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJlbWFpbCI6ImNoLmZpc2hAZ21haWwuY29tIiwiYXRfaGFzaCI6IkMxU1NxaUZsRl9JanZFNmh1eGVmSmciLCJlbWFpbF92ZXJpZmllZCI...),List(Transfer-Encoding: chunked, Vary: Origin,Accept-Encoding, Accept-Ranges: none, Alternate-Protocol: 443:quic,p=0.5, Server: GSE, X-XSS-Protection: 1; mode=block, X-Frame-Options: SAMEORIGIN, X-Content-Type-Options: nosniff, Content-Type: application/json; charset=UTF-8, Vary: X-Origin, Date: Sat, 11 Apr 2015 16:17:09 GMT, Expires: Fri, 01 Jan 1990 00:00:00 GMT, Pragma: no-cache, Cache-Control: no-cache, no-store, max-age=0, must-revalidate),HTTP/1.1)