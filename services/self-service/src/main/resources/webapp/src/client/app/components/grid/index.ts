import {NgModule, Component} from "@angular/core";
import {CommonModule} from "@angular/common";
import {Grid} from "./grid.component";
import { ResourcesModule } from './../list-of-resources/index';

export * from "./grid.component";

@NgModule({
  imports: [CommonModule, ResourcesModule],
  declarations: [Grid],
  exports: [Grid]
})

export class GridModule { }
