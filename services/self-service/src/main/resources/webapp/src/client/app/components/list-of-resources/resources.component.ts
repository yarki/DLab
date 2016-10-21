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
