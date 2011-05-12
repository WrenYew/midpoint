/*
 * Copyright (c) 2011 Evolveum
 *
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://www.opensource.org/licenses/cddl1 or
 * CDDLv1.0.txt file in the source code distribution.
 * See the License for the specific language governing
 * permission and limitations under the License.
 *
 * If applicable, add the following below the CDDL Header,
 * with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 *
 * Portions Copyrighted 2011 [name of copyright owner]
 * Portions Copyrighted 2010 Forgerock
 */

package com.evolveum.midpoint.test.ldap;

import java.io.File;

import java.net.URI;
import org.opends.messages.Message;
import org.opends.server.types.DirectoryEnvironmentConfig;
import org.opends.server.util.EmbeddedUtils;

/**
 * Represents the OpenDJ LDAP Server
 *
 * @author $author$
 * @version $Revision$ $Date$
 * @since 1.0.0
 */
public class OpenDJ {

    public static final String code_id = "$Id$";
    private URI serverRoot = null;

    public void intialize(URI serverRoot) {
        this.serverRoot = serverRoot;
    }

    public void startServer() throws Exception {
        DirectoryEnvironmentConfig config = new DirectoryEnvironmentConfig();
        config.setServerRoot(new File(serverRoot));
        config.setForceDaemonThreads(true);

        EmbeddedUtils.startServer(config);
    }

    public void stopServer() throws Exception {
        if (EmbeddedUtils.isRunning()) {
            EmbeddedUtils.stopServer(getClass().getName(), Message.EMPTY);
        }
    }

    public boolean isRunning() {
        return EmbeddedUtils.isRunning();
    }
}
