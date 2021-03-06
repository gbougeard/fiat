/*
 * Copyright 2016 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.netflix.spinnaker.fiat.providers;

import com.netflix.spinnaker.fiat.model.resources.Application;
import com.netflix.spinnaker.fiat.providers.internal.ClouddriverService;
import com.netflix.spinnaker.fiat.providers.internal.Front50Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import retrofit.RetrofitError;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class DefaultApplicationProvider extends BaseProvider<Application> implements ResourceProvider<Application> {

  private final Front50Service front50Service;

  private final ClouddriverService clouddriverService;

  @Autowired
  public DefaultApplicationProvider(Front50Service front50Service, ClouddriverService clouddriverService) {
    super();
    this.front50Service = front50Service;
    this.clouddriverService = clouddriverService;
  }

  @Override
  protected Set<Application> loadAll() throws ProviderException {
    try {
      Map<String, Application> appByName = front50Service
          .getAllApplicationPermissions()
          .stream()
          .collect(Collectors.toMap(Application::getName,
                                    Function.identity()));
      success();

      clouddriverService
          .getApplications()
          .stream()
          .filter(app -> !appByName.containsKey(app.getName()))
          .forEach(app -> appByName.put(app.getName(), app));
      success();

      return new HashSet<>(appByName.values());
    } catch (Exception e) {
      failure();
      throw e;
    }
  }
}
