/******************************************************************************************************

Copyright (c) 2016 EPAM Systems Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*****************************************************************************************************/
import {Observable} from "rxjs";
import {Response} from "@angular/http";
import {UserResourceService} from "../../services/userResource.service";

export class ComputationalResourcesModel {
  private notebook: any;
  private resource: any;
  private confirmAction : Function;
  private userResourceService : UserResourceService;
  public computationalName : string;
  constructor(notebook: any,
   			      resource: any,
              fnProcessResults: any,
              fnProcessErrors: any,
              userResourceService : UserResourceService
   			  ){
    this.userResourceService = userResourceService;
  	this.terminateComputationalResource(notebook, resource, fnProcessResults, fnProcessErrors);
  }


  terminateComputationalResource(notebook: any, resource: any,  fnProcessResults : any, fnProcessErrors: any){
    this.notebook = notebook;
    this.resource = resource;

    if(this.resource)
      this.computationalName = this.resource.computational_name

    this.confirmAction = () => this.userResourceService
      .suspendComputationalResource(notebook.name, resource.computational_name)
      .subscribe((response : Response) => fnProcessResults(response), (response: Response) => fnProcessErrors(response));
    };

   static getDefault (userResourceService : UserResourceService) : ComputationalResourcesModel {
     return new
       ComputationalResourcesModel({}, {}, () => {}, () => {}, userResourceService);
   }
}
