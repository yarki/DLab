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

import {Component, Input, Output} from "@angular/core";
import { UserResourceService } from "./../../services/userResource.service";

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

  constructor(
    private userResourceService: UserResourceService
    ) { }

  toggleResourceList() {
    this.collapse = !this.collapse;
  }

  printDetailResourceModal(data) {
    console.log(data);
  }

  terminateEmr(notebook, resource){
    let url = '/' + notebook.name + '/' + resource.computational_name + '/terminate';

    this.userResourceService
      .suspendComputationalResource(url)
      .subscribe((result) => {
        console.log('terminateEmr ', result);
      });
      return false;
  };
}
