package com.sixnations.rugby_blog.controller;

import com.intuit.karate.junit5.Karate;

class LoginIT{

    @Karate.Test
    Karate LoginIT() {
        return Karate.run("classpath:com/sixnations/rugby_blog/controller/login.feature");  // Path to the feature file
    }
}
