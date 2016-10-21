/******************************************************************************************************

Copyright (c) 2016 EPAM Systems Inc.

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

*****************************************************************************************************/

import {Component, Input, Output} from "@angular/core";

@Component({
    moduleId: module.id,
    selector: 'expanded-grid',
    templateUrl: 'resources.component.html',
    styleUrls: ['./resources.component.css']
})

export class ResourcesList {
  @Input() resources: any[];
  @Input() environment: any[];

  collapse: boolean = false;

  toggleResourceList() {
    this.collapse = !this.collapse;
  }

  printDetailResourceModal(data) {
    console.log(data);
  }

  resourceDecommission(parent_obj, resource) {
    console.log('Computational resources ' + resource.RESOURCE_NAME + ' ' + resource.NODES_NUMBER + ' nodes ' +  ' will be decommisioned. (ENVIRONMENT: ' + parent_obj.name + ')');
  }
}
