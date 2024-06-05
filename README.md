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

## License

```
greenDAO Build Tools
Copyright (C) 2016-2024 greenrobot (https://greenrobot.org)

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.
```
