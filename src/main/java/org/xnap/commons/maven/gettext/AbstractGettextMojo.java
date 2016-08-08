package org.xnap.commons.maven.gettext;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;

public abstract class AbstractGettextMojo extends AbstractMojo {


    /**
     * The output directory for generated class or properties files.
     */
    @Parameter(defaultValue = "${project.build.directory}/generated-sources/gettext", required = true)
    protected File outputDirectory;

    /**
     * Source directory. This directory is searched recursively for .java files.
     */
    @Parameter(defaultValue = "${project.build.sourceDirectory}", required = true)
    protected File sourceDirectory;
    
    /**
     * The output directory for the keys.pot directory for merging .po files. 
     */
    @Parameter(defaultValue = "${basedir}/src/main/po", required = true)
    protected File poDirectory;
    
    /**
     * Filename of the .pot file.
     */
    @Parameter(defaultValue = "keys.pot", required = true)
    protected String keysFile;

    /**
     * Optional raw command line arguments
     */
    @Parameter
    protected String[] extraArguments;

    /**
     * Deactivates code omitLocation information if set to true.
     */
    @Parameter(defaultValue = "false")
    protected boolean omitLocation;

    protected void addExtraArguments(Commandline cl) {
        if (extraArguments != null) {
            for (String argument : extraArguments) {
                cl.createArg().setValue(argument);
            }
        }
    }
}
