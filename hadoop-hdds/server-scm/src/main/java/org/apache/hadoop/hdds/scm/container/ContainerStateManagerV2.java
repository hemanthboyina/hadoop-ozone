/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership.  The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.hadoop.hdds.scm.container;

import java.io.IOException;
import java.util.Set;

import org.apache.hadoop.hdds.protocol.proto.HddsProtos.ContainerInfoProto;
import org.apache.hadoop.hdds.protocol.proto.HddsProtos.LifeCycleState;
import org.apache.hadoop.hdds.scm.metadata.Replicate;

/**
 * A ContainerStateManager is responsible for keeping track of all the
 * container and its state inside SCM, it also exposes methods to read and
 * modify the container and its state.
 *
 * All the mutation operations are marked with {@link Replicate} annotation so
 * that when SCM-HA is enabled, the mutations are replicated from leader SCM
 * to the followers.
 *
 * When a method is marked with {@link Replicate} annotation it should follow
 * the below rules.
 *
 * 1. The method call should be Idempotent
 * 2. Arguments should be of protobuf objects
 * 3. Return type should be of protobuf object
 * 4. The declaration should throw RaftException
 *
 */
public interface ContainerStateManagerV2 {

  //TODO: Rename this to ContainerStateManager

  /* **********************************************************************
   * Container Life Cycle                                                 *
   *                                                                      *
   * Event and State Transition Mapping:                                  *
   *                                                                      *
   * State: OPEN         ----------------> CLOSING                        *
   * Event:                    FINALIZE                                   *
   *                                                                      *
   * State: CLOSING      ----------------> QUASI_CLOSED                   *
   * Event:                  QUASI_CLOSE                                  *
   *                                                                      *
   * State: CLOSING      ----------------> CLOSED                         *
   * Event:                     CLOSE                                     *
   *                                                                      *
   * State: QUASI_CLOSED ----------------> CLOSED                         *
   * Event:                  FORCE_CLOSE                                  *
   *                                                                      *
   * State: CLOSED       ----------------> DELETING                       *
   * Event:                    DELETE                                     *
   *                                                                      *
   * State: DELETING     ----------------> DELETED                        *
   * Event:                    CLEANUP                                    *
   *                                                                      *
   *                                                                      *
   * Container State Flow:                                                *
   *                                                                      *
   * [OPEN]--------------->[CLOSING]--------------->[QUASI_CLOSED]        *
   *          (FINALIZE)      |      (QUASI_CLOSE)        |               *
   *                          |                           |               *
   *                          |                           |               *
   *                  (CLOSE) |             (FORCE_CLOSE) |               *
   *                          |                           |               *
   *                          |                           |               *
   *                          +--------->[CLOSED]<--------+               *
   *                                        |                             *
   *                                (DELETE)|                             *
   *                                        |                             *
   *                                        |                             *
   *                                   [DELETING]                         *
   *                                        |                             *
   *                              (CLEANUP) |                             *
   *                                        |                             *
   *                                        V                             *
   *                                    [DELETED]                         *
   *                                                                      *
   ************************************************************************/

  /**
   * Returns a new container ID which can be used for allocating a new
   * container.
   */
  ContainerID getNextContainerID();

  /**
   * Returns the ID of all the managed containers.
   *
   * @return Set of {@link ContainerID}
   */
  Set<ContainerID> getContainerIDs();

  /**
   *
   */
  Set<ContainerID> getContainerIDs(LifeCycleState state);

  /**
   *
   */
  ContainerInfo getContainer(ContainerID containerID)
      throws ContainerNotFoundException;

  /**
   *
   */
  Set<ContainerReplica> getContainerReplicas(ContainerID containerID)
      throws ContainerNotFoundException;

  /**
   *
   */
  @Replicate
  void addContainer(ContainerInfoProto containerInfo)
      throws IOException;

  /**
   *
   */
  void close() throws Exception;
}
