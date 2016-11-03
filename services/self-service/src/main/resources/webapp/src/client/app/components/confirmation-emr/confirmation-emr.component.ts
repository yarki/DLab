/******************************************************************************************************

 Copyright (c) 2016 EPAM Systems Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *****************************************************************************************************/

import { Component, OnInit, ViewChild, Input, Output, EventEmitter } from '@angular/core';
import { UserResourceService } from "./../../services/userResource.service";
import {Response} from "@angular/http";
import HTTP_STATUS_CODES from 'http-status-enum';

 @Component({
   moduleId: module.id,
   selector: 'confirmation-emr',
   templateUrl: 'confirmation-emr.component.html'
 })

 export class ConfirmationEmr {
   notebook: Object;
   resource: Object;

   @ViewChild('bindDialog') bindDialog;
   constructor(
    private userResourceService: UserResourceService
    ) { }

   open(option, notebook, resource) {
     this.notebook = notebook;
     this.resource = resource;
     this.bindDialog.open(option);
   }
   close() {
     this.bindDialog.close();
   }
   terminateEmr(notebook, resource){
    this.userResourceService
      .suspendComputationalResource(notebook.name, resource.computational_name)
      .subscribe((result) => {
        console.log('terminateEmr ', result);
        this.bindDialog.close();
      });
      return false;
   };
 }
