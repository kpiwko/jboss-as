/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.jboss.as.host.controller.operations;


import org.jboss.as.controller.extension.ExtensionRegistry;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.OperationStepHandler;
import org.jboss.as.controller.PathAddress;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.DOMAIN_CONTROLLER;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.LOCAL;
import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.REMOTE;

import java.util.Locale;

import org.jboss.as.controller.descriptions.DescriptionProvider;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.registry.Resource;
import org.jboss.as.domain.controller.DomainController;
import org.jboss.as.domain.controller.DomainModelUtil;
import org.jboss.as.domain.controller.UnregisteredHostChannelRegistry;
import org.jboss.as.host.controller.HostControllerConfigurationPersister;
import org.jboss.as.host.controller.descriptions.HostRootDescription;
import org.jboss.as.repository.ContentRepository;
import org.jboss.as.repository.HostFileRepository;
import org.jboss.dmr.ModelNode;

/**
 * @author <a href="kabir.khan@jboss.com">Kabir Khan</a>
 * @author <a href="mailto:darran.lofthouse@jboss.com">Darran Lofthouse</a>
 * @version $Revision: 1.1 $
 */
public class LocalDomainControllerAddHandler implements OperationStepHandler, DescriptionProvider {

    public static final String OPERATION_NAME = "write-local-domain-controller";

    private final ManagementResourceRegistration rootRegistration;
    private final HostControllerConfigurationPersister overallConfigPersister;
    private final HostFileRepository fileRepository;
    private final LocalHostControllerInfoImpl hostControllerInfo;
    private final ContentRepository contentRepository;
    private final DomainController domainController;
    private final UnregisteredHostChannelRegistry channelRegistry;
    private final ExtensionRegistry extensionRegistry;

    public static LocalDomainControllerAddHandler getInstance(final ManagementResourceRegistration rootRegistration,
                                                                 final LocalHostControllerInfoImpl hostControllerInfo,
                                                                 final HostControllerConfigurationPersister overallConfigPersister,
                                                                 final HostFileRepository fileRepository,
                                                                 final ContentRepository contentRepository,
                                                                 final DomainController domainController,
                                                                 final UnregisteredHostChannelRegistry channelRegistry,
                                                                 final ExtensionRegistry extensionRegistry) {
        return new LocalDomainControllerAddHandler(rootRegistration, hostControllerInfo, overallConfigPersister,
                fileRepository, contentRepository, domainController, channelRegistry, extensionRegistry);
    }

    protected LocalDomainControllerAddHandler(final ManagementResourceRegistration rootRegistration,
                                    final LocalHostControllerInfoImpl hostControllerInfo,
                                    final HostControllerConfigurationPersister overallConfigPersister,
                                    final HostFileRepository fileRepository,
                                    final ContentRepository contentRepository,
                                    final DomainController domainController,
                                    final UnregisteredHostChannelRegistry channelRegistry,
                                    final ExtensionRegistry extensionRegistry) {
        this.rootRegistration = rootRegistration;
        this.overallConfigPersister = overallConfigPersister;
        this.fileRepository = fileRepository;
        this.hostControllerInfo = hostControllerInfo;
        this.contentRepository = contentRepository;
        this.domainController = domainController;
        this.channelRegistry = channelRegistry;
        this.extensionRegistry = extensionRegistry;
    }

    @Override
    public void execute(OperationContext context, ModelNode operation) throws OperationFailedException {
        final Resource resource = context.readResourceForUpdate(PathAddress.EMPTY_ADDRESS);
        final ModelNode model = resource.getModel();

        ModelNode dc = model.get(DOMAIN_CONTROLLER);
        dc.get(LOCAL).setEmptyObject();

        if (dc.has(REMOTE)) {
            dc.remove(REMOTE);
        }

        initializeDomain();

        context.completeStep(OperationContext.RollbackHandler.NOOP_ROLLBACK_HANDLER);
    }

    protected void initializeDomain() {
        hostControllerInfo.setMasterDomainController(true);
        overallConfigPersister.initializeDomainConfigurationPersister(false);

        DomainModelUtil.initializeMasterDomainRegistry(rootRegistration, overallConfigPersister.getDomainPersister(),
                contentRepository, fileRepository, domainController, channelRegistry, extensionRegistry);
    }


    //Done by DomainModelControllerService
//    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model,
//                                  ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers) {
//        final ModelNode hostModel = context.readModel(PathAddress.EMPTY_ADDRESS);
//        final ServiceTarget serviceTarget = context.getServiceTarget();
//        newControllers.addAll(installLocalDomainController(hostModel, serviceTarget, false, verificationHandler));
//    }

    @Override
    public ModelNode getModelDescription(final Locale locale) {
        return HostRootDescription.getLocalDomainControllerAdd(locale);
    }
}
