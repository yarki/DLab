/***************************************************************************

Copyright (c) 2016, EPAM SYSTEMS INC

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

****************************************************************************/

import { Injectable } from '@angular/core';
import {Http, Response, RequestOptions, RequestMethod, Headers} from '@angular/http';
import {Observable} from "rxjs";
import {Dictionary} from "../util/collections/dictionary/dictionary";

@Injectable()
export class ApplicationServiceFacade {

  private static readonly LOGIN = "login";
  private static readonly LOGOUT = "logout";
  private static readonly AUTHORIZE = "authorize";
  private static readonly ACCESS_KEY = "access_key";
  private static readonly PROVISIONED_RESOURCES = "provisioned_resources";
  private static readonly EXPLORATORY_ENVIRONMENT = "exploratory_environment";
  private static readonly EXPLORATORY_ENVIRONMENT_TEMPLATES = "exploratory_environment_templates";
  private static readonly COMPUTATIONAL_RESOURCES_TEMLATES = "computational_resources_templates";
  private static readonly COMPUTATIONAL_RESOURCES_SHAPES = "computational_resources_shapes";
  private static readonly COMPUTATIONAL_RESOURCES = "computational_resources";
  private accessTokenKey : string = "access_token";
  private requestRegistry : Dictionary<string>;

  constructor(private http: Http) {
    this.setupRegistry();
  }

  private setupRegistry () : void {
    this.requestRegistry = new Dictionary<string>();

    // Security

    this.requestRegistry.Add(ApplicationServiceFacade.LOGIN, "/api/user/login");
    this.requestRegistry.Add(ApplicationServiceFacade.LOGOUT, "/api/user/logout");
    this.requestRegistry.Add(ApplicationServiceFacade.AUTHORIZE, "/api/user/authorize");
    this.requestRegistry.Add(ApplicationServiceFacade.ACCESS_KEY, "/api/user/access_key");

    // Exploratory Environment

    this.requestRegistry.Add(ApplicationServiceFacade.PROVISIONED_RESOURCES, "/api/infrastructure_provision/provisioned_user_resources");
    this.requestRegistry.Add(ApplicationServiceFacade.EXPLORATORY_ENVIRONMENT, "/api/infrastructure_provision/exploratory_environment");
    this.requestRegistry.Add(ApplicationServiceFacade.EXPLORATORY_ENVIRONMENT_TEMPLATES, "/api/infrastructure_provision/exploratory_environment_templates");


    // Computational Resources
    this.requestRegistry.Add(ApplicationServiceFacade.COMPUTATIONAL_RESOURCES, "/api/infrastructure_provision/computational_resources");
    this.requestRegistry.Add(ApplicationServiceFacade.COMPUTATIONAL_RESOURCES_TEMLATES, "/api/infrastructure_provision/computational_resources_templates");
    this.requestRegistry.Add(ApplicationServiceFacade.COMPUTATIONAL_RESOURCES_SHAPES, "/api/infrastructure_provision/computational_resources_shapes");
  }

  private buildRequest(method : RequestMethod, url : string, body : any, opt : RequestOptions) : Observable<Response> {
    if(method == RequestMethod.Post)
      return this.http.post(url, body, opt);
      else if (method == RequestMethod.Delete)
        return this.http.delete(body ? url + JSON.parse(body) : url, opt);
      else if(method == RequestMethod.Put)
        return this.http.put(url, body, opt);
    else return this.http.get(url, opt);
  }

  private getRequestOptions(json:boolean, auth:boolean) : RequestOptions {
    let headers = new Headers();
    if(json)
      headers.append('Content-type', 'application/json; charset=utf-8');
    if(auth)
      headers.append("Authorization", "Bearer " + localStorage.getItem(this.accessTokenKey));
    let reqOpt = new RequestOptions({ headers: headers });
    return reqOpt;
  }

  buildLoginRequest(body: any) : Observable<Response> {
    return this.buildRequest(RequestMethod.Post,
      this.requestRegistry.Item(ApplicationServiceFacade.LOGIN),
      body,
      this.getRequestOptions(true, false));
  }

  buildLogoutRequest(body: any) : Observable<Response>  {
    return this.buildRequest(RequestMethod.Post,
      this.requestRegistry.Item(ApplicationServiceFacade.LOGOUT),
      body,
      this.getRequestOptions(true, true));
  }

  buildAuthorizeRequest(body:any) : Observable<Response> {
    return this.buildRequest(RequestMethod.Post,
      this.requestRegistry.Item(ApplicationServiceFacade.AUTHORIZE),
      body,
      this.getRequestOptions(true, true));
  }

  buildCheckUserAccessKeyRequest() : Observable<Response> {
    return this.buildRequest(RequestMethod.Get,
      this.requestRegistry.Item(ApplicationServiceFacade.ACCESS_KEY),
      null,
      this.getRequestOptions(true, true));
  }

  buildUploadUserAccessKeyRequest(body: any) : Observable<Response> {
    return this.buildRequest(RequestMethod.Post,
      this.requestRegistry.Item(ApplicationServiceFacade.ACCESS_KEY),
      body,
      this.getRequestOptions(false, true));
  }

  buildGetUserProvisionedResourcesRequest() : Observable<Response> {
    return this.buildRequest(RequestMethod.Get,
      this.requestRegistry.Item(ApplicationServiceFacade.PROVISIONED_RESOURCES),
      null,
      this.getRequestOptions(true, true));
  }

  buildGetSupportedComputationalResourcesShapesRequest() : Observable<Response> {
    return this.buildRequest(RequestMethod.Get,
      this.requestRegistry.Item(ApplicationServiceFacade.COMPUTATIONAL_RESOURCES_SHAPES),
      null,
      this.getRequestOptions(true, true));
  }

  buildGetExploratoryEnvironmentTemplatesRequest() : Observable<Response> {
    return this.buildRequest(RequestMethod.Get,
      this.requestRegistry.Item(ApplicationServiceFacade.EXPLORATORY_ENVIRONMENT_TEMPLATES),
      null,
      this.getRequestOptions(true, true));
  }

  buildGetComputationalResourcesTemplatesRequest() : Observable<Response> {
    return this.buildRequest(RequestMethod.Get,
      this.requestRegistry.Item(ApplicationServiceFacade.COMPUTATIONAL_RESOURCES_TEMLATES),
      null,
      this.getRequestOptions(true, true));
  }

  buildCreateExploratoryEnvironmentRequest(data) : Observable<Response> {
    return this.buildRequest(RequestMethod.Put,
      this.requestRegistry.Item(ApplicationServiceFacade.EXPLORATORY_ENVIRONMENT),
      data,
      this.getRequestOptions(true, true));
  }

  buildRunExploratoryEnvironmentRequest(data) : Observable<Response> {
    return this.buildRequest(RequestMethod.Post,
      this.requestRegistry.Item(ApplicationServiceFacade.EXPLORATORY_ENVIRONMENT),
      data,
      this.getRequestOptions(true, true));
  }

  buildSuspendExploratoryEnvironmentRequest(data) : Observable<Response> {
    return this.buildRequest(RequestMethod.Delete,
      this.requestRegistry.Item(ApplicationServiceFacade.EXPLORATORY_ENVIRONMENT),
      data,
      this.getRequestOptions(true, true));
  }

  buildCreateComputationalResourcesRequest(data) : Observable<Response> {
    return this.buildRequest(RequestMethod.Put,
      this.requestRegistry.Item(ApplicationServiceFacade.COMPUTATIONAL_RESOURCES),
      data,
      this.getRequestOptions(true, true));
  }

  buildDeleteComputationalResourcesRequest(data) : Observable<Response> {
    return this.buildRequest(RequestMethod.Delete,
      this.requestRegistry.Item(ApplicationServiceFacade.COMPUTATIONAL_RESOURCES),
      data,
      this.getRequestOptions(true, true));
  }
}
