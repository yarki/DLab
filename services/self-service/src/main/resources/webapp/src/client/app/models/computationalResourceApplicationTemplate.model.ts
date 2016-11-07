import  { ComputationalResourceApplication } from "./computationalResourceApplication.model";
import { ResourceShapeModel } from "./resourceShape.model";

export class ComputationalResourceApplicationTemplate {
  version: string;
  applications: Array<ComputationalResourceApplication>
  shapes: Array<ResourceShapeModel>;
  
  constructor(jsonModel:any, shapes: Array<ResourceShapeModel>) {
    this.version = jsonModel.version;
    this.applications = [];
    this.shapes = shapes;

    if(jsonModel.applications && jsonModel.applications.length > 0)
      for (let index = 0; index < jsonModel.applications.length; index++)
        this.applications.push(new ComputationalResourceApplication(jsonModel.applications[index]));

  }
}
