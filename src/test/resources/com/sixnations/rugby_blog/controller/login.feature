Feature: Login Test for Admin User

Background:
  * url 'http://localhost:8081'

Scenario: Get authentication token for admin user
  Given path '/api/auth/login'
  And request { identifier: 'cdb97', password: 'cdb97@' }
  When method POST
  Then status 200
  * print 'Full Response:', response
  And match response != null
  And match response != ''
  * def authToken = response
  * karate.set('authToken', authToken)
  * print 'Generated Token:', karate.get('authToken')


