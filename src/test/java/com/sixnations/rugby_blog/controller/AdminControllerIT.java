package com.sixnations.rugby_blog.controller;

import com.intuit.karate.junit5.Karate;

class AdminControllerIT{

    @Karate.Test
    Karate AdminControllerIT() {
        return Karate.run("classpath:com/sixnations/rugby_blog/controller/admin.feature");  // Path to the feature file
    }
}

