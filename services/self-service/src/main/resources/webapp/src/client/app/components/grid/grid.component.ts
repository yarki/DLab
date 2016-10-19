import { Component, EventEmitter, Input, Output } from "@angular/core";
import { EnvironmentsService } from './../../services/environments.service';
import { GridModel } from './grid.model';

@Component({
  moduleId: module.id,
  selector: 'ng-grid',
  templateUrl: 'grid.component.html',
  styleUrls: ['./grid.component.css']
})

export class Grid {
  public environments: any;
  // environments: Array<GridModel>;


  constructor(private environmentsService: EnvironmentsService) {
    // this.environments = new GridModel<any>([]);
  }

  ngOnInit() {
    this.environmentsService.getEnvironmentsList().subscribe((list) => {
      this.environments = list['resources'];
    });
  }
}
