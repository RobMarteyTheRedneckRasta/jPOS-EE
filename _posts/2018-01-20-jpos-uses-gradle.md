---
ID: 233
post_title: jPos uses Gradle
author: admin
post_excerpt: ""
layout: post
permalink: >
  http://cannabisys.com/cannapos-requirements/jpos-uses-gradle/
published: true
post_date: 2018-01-20 06:29:54
---
<strong>jPos uses Gradle.</strong>

I'm building a test environment on a linux Os. So I took the easy way out and apt installed it.
Running the apt install gradle command completed the task successfully, for me. But, I do expect to have to manually update, or if issues arise, do as jPos.org states, and pull the package from gradle.org's website.

If you prefer to take the manual approach, you can download the package from the <a href="http://www.gradle.org/" target="_blank" rel="noopener">Gradle website</a> with a multi-module setup.
The modules are defined in the settings.gradle file and listed below:
• jpos : this is the jPOS system
• compat_1_5_2 : compatibility with older versions
• jPOS sources in the jpos/src directory.

&nbsp;

cc/