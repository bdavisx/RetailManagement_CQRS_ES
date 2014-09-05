# Introduction to the Project – CQRS with Event Sourcing – Retail Management Application

We're going to work on an application to study the [Axon framework](http://www.axonframework.org/) along
    with CQRS and Event Sourcing. I'm going to create pieces of a retail management application to explore CQRS.
    Remember that this is an exploration. I'm going to work on the project when I have spare time. It's supposed to be a
    learning experience for both myself an anyone reading this. I'm going to document my thoughts as I go along, why I
    am doing something with respect to design and development principles. This isn't meant to be training so much as a
    journal. I'm not making any assumptions as far as what my audience level might be, so I may point out some stuff
    that is very basic at times.

You should know Java from a simple server side standpoint - the concept of Servlets and JSPs (you don't need to be a
    JEE expert). I'm going to assume you know the basics of CQRS and Event Sourcing. If not, [start here](http://cqrs.wordpress.com/documents/) and try Googling both terms. You should also read the
    [Axon quick start guide](http://www.axonframework.org/axon-2-quickstart-guide/) before going any further,
    if you haven't already. [http://www.axonframework.org/axon-2-quickstart-guide/](http://www.axonframework.org/axon-2-quickstart-guide/).

I don't really know retail management, so I'm going to go from feature lists from other applications, open source or
    otherwise. I'm mentioning this because if you have some real retail application development experience, I might make
    some kind of assumption that's completely wrong, but at least you'll know why. But retail is an area we all have
    experience with from real life. Beyond just shopping, I did work in a couple of retail stores as well as a
    warehouse when I was going through college. I'm not pretending to know retail in-depth. I'm always open to
    suggestions for something that could be done better. While I'm not aiming for a "real" retail management
    application, I do want the parts that I create to be as realistic as possible. 

What I do create will be open source (the github repository is [https://github.com/bdavisx/RetailManagement_CQRS_ES](https://github.com/bdavisx/RetailManagement_CQRS_ES)
    and hopefully a learning experience for anyone who finds this blog. I'm going to be developing with [IntelliJ IDEA](http://www.jetbrains.com/idea/) for writing the code and [Gradle](http://www.gradle.org/) for the builds. I'll be using [Tomcat](http://tomcat.apache.org/)
    for the web container, if it appears that a full blown application server is needed, we'll switch over to [JBoss](https://www.jboss.org/overview/). I'm not an expert in Gradle, I've done a lot more in Maven,
    but I'm using this project to catch up on some areas that I feel I'm lacking in. 

For the UI parts I'll be using [Spring MVC](http://docs.spring.io/spring/docs/current/spring-framework-reference/html/mvc.html) with [Thymeleaf](http://www.thymeleaf.org/), I might throw in some [AngularJS ](http://angularjs.org/)as
    well if it seems like a single page application might be a better fit than request/response. Even with that, I'll
    still be using Spring MVC to create the REST for Angular. Some pieces of retail management UI may need to be in a
    kiosk format. If you're doing point-of-sale stuff, it's not going to work as a regular web application. I know you
    can do a lot with HTML5, but a real point-of-sale system has cash registers and such. You're going to need to be
    able to interface with the cash drawer, bar code scanners and possibly other pieces of hardware. So we might be
    looking at some kind of GUI - since we're using Java (or at least JVM based languages), this means JavaFX, Swing or
    SWT. There are other ways of creating a GUI in Java, but those are the top frameworks. If we do anything that is GUI
    based, I'm planning on using JavaFX since this is supposed to be a learning project, and that's the framework I've
    had the least experience with. Since we're really wanting to learn and build the CQRS part of the application, we
    may not need to worry about the GUI. Our design would likely have a GUI communicating with REST to a back end, so we
    may just ignore the front end as much as possible. I haven't really decided yet.

For the data side, [Mongo](http://www.mongodb.org/) for the event sourcing and [PostgreSQL](http://www.postgresql.org/) for the query side. The reason I'm using separate databases is that
    in a lot of enterprises, you're going to need to be able to end up with data in a relational database. 

So that's the basic plan. Like any project, it could change, so we'll see where it goes.

