/******************************************************************************************************

 Copyright (c) 2016 EPAM Systems Inc.

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 *****************************************************************************************************/

import { Component, OnInit, ViewChild, Input, Output, EventEmitter } from '@angular/core';
import { Response } from "@angular/http";
import { UserResourceService } from "../../services/userResource.service";
import { ComputationalResourcesModel } from "./confirmation-computational-resources.model";

 @Component({
   moduleId: module.id,
   selector: 'confirmation-computational-resources',
   templateUrl: 'confirmation-computational-resources.component.html'
 })

 export class ConfirmationComputationalResources {
   model : ComputationalResourcesModel;

   @ViewChild('bindDialog') bindDialog;
   @Output() rebuildGrid: EventEmitter<{}> = new EventEmitter();

   constructor(private userResourceService: UserResourceService) { }

   public open(option, notebook, resource) {
     this.model = new ComputationalResourcesModel(notebook, resource,
       (response: Response) => {
         this.close();
         this.rebuildGrid.emit();
     },
     (response : Response) => console.error(response.status),
     this.userResourceService);
     
     if(!this.bindDialog.isOpened) {
       this.bindDialog.open(option);
     }
   }

   public close() {
     if(this.bindDialog.isOpened)
      this.bindDialog.close();
   }
 }
