
Feature: Admin User Management with Authentication

Background:
  * url 'http://localhost:8081'

Feature: Admin User Management with Authentication

Background:
  * url 'http://localhost:8081'
  * def authToken = null  # Initialize a global token variable

Scenario: Get authentication token for admin user
  Given path '/api/auth/login'
  And request { identifier: 'cdb97', password: 'cdb97@' }
  When method POST
  Then status 200
  * def authToken = response.token  # Store the token in the global variable
  * karate.set('authToken', authToken)  # Save it globally

Scenario: Admin can promote a user to admin
  Given header Authorization = 'Bearer ' + karate.get('authToken')  # Retrieve the token
  And path 'api/admin/promote/1'
  When method PUT
  Then status 200
  And match response == 'User promoted to Admin successfully.'

Scenario: Non-admin cannot promote a user
  Given header Authorization = 'Bearer ' + karate.get('authToken')
  And path 'api/admin/promote/1'
  When method PUT
  Then status 403
  And match response == 'Forbidden'

Scenario: Trying to promote a user who is already an admin
  Given header Authorization = 'Bearer ' + karate.get('authToken')
  And path 'api/admin/promote/1'
  When method PUT
  Then status 400
  And match response == 'User not found or already an Admin.'

Scenario: Admin can suspend a user
  Given header Authorization = 'Bearer ' + karate.get('authToken')
  And path 'api/admin/suspend/2'
  When method PUT
  Then status 200
  And match response == 'User suspended successfully.'
