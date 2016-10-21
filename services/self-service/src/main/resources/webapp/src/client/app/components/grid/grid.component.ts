import { Component, EventEmitter, Input, Output } from "@angular/core";
import { EnvironmentsService } from './../../services/environments.service';
import { GridRowModel } from './grid.model';

@Component({
  moduleId: module.id,
  selector: 'ng-grid',
  templateUrl: 'grid.component.html',
  styleUrls: ['./grid.component.css']
})

export class Grid {

  list: Array<any>;
  environments: Array<GridRowModel>;

  constructor(private environmentsService: EnvironmentsService) { }

  ngOnInit() {
    this.environmentsService.getEnvironmentsList().subscribe((list) => {
      this.list = list['RESOURCES'];

      this.environments = this.loadEnvironments();
      console.log('models ', this.environments);
    });
  }

  loadEnvironments(): Array<any> {
    return this.list.map((value) => {
      return new GridRowModel(value.ENVIRONMENT_NAME,
        value.STATUS,
        value.SHAPE,
        value.COMPUTATIONAL_RESOURCES);
    });
  }

  printDetailEnvironmentModal(data) {
    console.log(data);
  }

  mathAction(data, action) {
    console.log('action ' + action, data);
  }
}
