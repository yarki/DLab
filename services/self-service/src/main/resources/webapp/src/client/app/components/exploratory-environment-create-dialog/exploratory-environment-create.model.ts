/******************************************************************************************************

 Copyright (c) 2016 EPAM Systems Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *****************************************************************************************************/

import { Observable } from "rxjs";
import { Response } from "@angular/http";
import { UserResourceService } from "../../services/userResource.service";
import HTTP_STATUS_CODES from 'http-status-enum';

export class ExploratoryEnvironmentCreateModel {

  confirmAction: Function;
  private environment_name: string;
  private environment_version: string;
  private environment_shape: string;
  private userResourceService: UserResourceService;

  constructor(
    environment_name: string,
    environment_version: string,
    environment_shape: string,
    fnProcessResults: any,
    fnProcessErrors: any,
    userResourceService: UserResourceService
  ) {
    this.userResourceService = userResourceService;
    this.prepareModel(environment_name, environment_version, environment_shape, fnProcessResults, fnProcessErrors);
  }

  static getDefault(): ExploratoryEnvironmentCreateModel {
    return new ExploratoryEnvironmentCreateModel('', '', '', () => { }, () => { }, null);
  }

  private createExploratoryEnvironment(): Observable<Response> {
    return this.userResourceService.createExploratoryEnvironment({
      name: this.environment_name,
      shape: this.environment_shape,
      version: this.environment_version
    });
  }

  private prepareModel(environment_name: string, environment_version: string, environment_shape: string, fnProcessResults: any, fnProcessErrors: any): void {

    this.setCreatingParams(environment_version, environment_name, environment_shape)
    this.confirmAction = () => this.createExploratoryEnvironment()
      .subscribe(
      (response: Response) => fnProcessResults(response));
  }

  setCreatingParams(version, name, shape) {
    this.environment_version = version;
    this.environment_name = name;
    this.environment_shape = shape;
  }
}
