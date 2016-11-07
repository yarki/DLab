export class ComputationalResourceApplication {
  name: string;
  version: string;

  constructor(jsonModel:any) {
    this.name = jsonModel.name;
    this.version = jsonModel.version;
  }
}
