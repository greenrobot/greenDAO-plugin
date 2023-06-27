# greenDAO Gradle plugin

## Publish to Maven Central

In global `gradle.properties` configure:
```
# preferredRepo=local
sonatypeUsername=<TODO>
sonatypePassword=<TODO>
```

Then run upload tasks for projects that should be published, e.g.
```
:greendao-code-modifier:uploadArchives
:greendao-gradle-plugin:uploadArchives
```

Then go to https://oss.sonatype.org/#stagingRepositories and close and release the staging repository.
