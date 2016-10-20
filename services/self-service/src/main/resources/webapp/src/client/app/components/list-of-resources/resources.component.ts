import {Component, Input, Output} from "@angular/core";

@Component({
    moduleId: module.id,
    selector: 'expanded-grid',
    templateUrl: 'resources.component.html',
    styleUrls: ['./resources.component.css']
})

export class ResourcesList {
  @Input() resources: any[];

  collapse: boolean = false;

  toggleResourceList() {
    this.collapse = !this.collapse;
  }
}
