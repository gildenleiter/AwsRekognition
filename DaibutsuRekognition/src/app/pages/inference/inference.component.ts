import { Component, OnInit, OnDestroy } from '@angular/core';
import {AwsService} from '../../service/aws.service'
import { interval, Subscription } from 'rxjs';
import * as StatusCustomModel from '../../const/awsStatusCustomModel';

@Component({
  selector: 'app-inference',
  templateUrl: './inference.component.html',
  styleUrls: ['./inference.component.css']
})
export class InferenceComponent implements OnInit, OnDestroy {

  private subscription!: Subscription;
  file: any = null;
  imgSrc: any | ArrayBuffer = "";
  listProject: any = null;

  constructor(private awsService: AwsService) { }

  ngOnDestroy() {
    if(this.subscription != null){
      this.subscription.unsubscribe();
    }
  }

  ngOnInit() {
    // 初期表示処理
    var startButtonElemet = document.getElementById("startCustomModel") as HTMLButtonElement;
    var stopButtonElemet = document.getElementById("stopCustomModel") as HTMLButtonElement;
    var playButtonElemet = document.getElementById("playCustomModel") as HTMLButtonElement;
    startButtonElemet.disabled = true;
    stopButtonElemet.disabled = true;
    playButtonElemet.disabled = true;
    
    // プロジェクト一覧取得
    this.awsService.getProjectAll().subscribe(x => { 
      this.listProject = x;

      var listProjectElement = document.getElementById("listProject");

      if(listProjectElement != undefined) {
        for (let i = 0; i < x.length; i++) {
          // optionタグを作成する
          var option = document.createElement("option");
          option.text = x[i].id;
          option.value = x[i].id;
          // selectタグの子要素にoptionタグを追加する
          listProjectElement.appendChild(option);
        }
      }

      if(listProjectElement != undefined) {
        var projectId = this.getProjectId();

        if(this.existVersion(projectId)){
          this.subscription = interval(1000)
          .subscribe((X: any) => { this.getStatusCustomModel(projectId); });
        } else {
          if(this.subscription != undefined){
            this.subscription.unsubscribe();
          }

          var element = document.getElementById("status");
          if(element != undefined) {
            element.innerHTML = "ProjectName NULL";
          }

          var startButtonElemet = document.getElementById("startCustomModel") as HTMLButtonElement;
          var stopButtonElemet = document.getElementById("stopCustomModel") as HTMLButtonElement;
          var playButtonElemet = document.getElementById("playCustomModel") as HTMLButtonElement;

          startButtonElemet.disabled = true;
          stopButtonElemet.disabled = true;
          playButtonElemet.disabled = true;
        }


      }
    });
  }

  onChangeProjectId(){
    var projectId = this.getProjectId();
    
    if (this.subscription) {
      if(this.subscription != undefined){
        this.subscription.unsubscribe();
      }

      var element = document.getElementById("status");
      if(element != undefined) {
        element.innerHTML = "ProjectName NULL";
      }

      var startButtonElemet = document.getElementById("startCustomModel") as HTMLButtonElement;
      var stopButtonElemet = document.getElementById("stopCustomModel") as HTMLButtonElement;
      var playButtonElemet = document.getElementById("playCustomModel") as HTMLButtonElement;

      startButtonElemet.disabled = true;
      stopButtonElemet.disabled = true;
      playButtonElemet.disabled = true;
    }

    if(this.existVersion(projectId)){
      this.subscription = interval(1000)
        .subscribe((X: any) => { this.getStatusCustomModel(projectId); });
    }
  }

  onChangeFileInput(event:any) {

    //fileが選択されていなければリセット
    if (event.target.files.length === 0) {
      this.file = null;
      this.imgSrc = "";
      return;
    }

    //ファイルの情報をfileとimgSrcに保存
    let reader = new FileReader();
    this.file = event.target.files[0];
    reader.onload = () => {
      this.imgSrc = reader.result;
    }
    reader.readAsDataURL(this.file);
  }

  test(){
    this.awsService.test().subscribe(x => { 
        console.log('Subscriber:', x);
      }
    );
  }

  onS3Upload(){
    this.awsService.uploadS3(this.file).subscribe(x => { 
        console.log('Subscriber:', x);
        var element = document.getElementById("resultLog");
        if(element != undefined) {
          element.innerHTML = x;
        }
      }
    );
  }

  onStartCustomModel(){
    var projectId = this.getProjectId();

    this.awsService.startCustomModel(projectId).subscribe(x => {
        console.log('Subscriber:', x);
        var element = document.getElementById("resultLog");
        if(element != undefined) {
          element.innerHTML = x;
        }
      }
    );
  }

  onStopCustomModel(){
    var projectId = this.getProjectId();

    this.awsService.stopCustomModel(projectId).subscribe(x => {
        console.log('Subscriber:', x);
        var element = document.getElementById("resultLog");
        if(element != undefined) {
          element.innerHTML = x;
        }
      }
    );
  }

  onCustomModel(){
    var projectId = this.getProjectId();

    this.awsService.customModel(projectId, this.file).subscribe(x => { 
        console.log('Subscriber:', x);
        var element = document.getElementById("resultLog");
        if(element != undefined) {
          element.innerHTML = x;
        }
      }
    );
  }

  private getStatusCustomModel (projectId:string) {
    this.awsService.getStatusCustomModel(projectId).subscribe(x => {
        var status = x;
        console.log('Subscriber:', status);
        var element = document.getElementById("status");
        var startButtonElemet = document.getElementById("startCustomModel") as HTMLButtonElement;
        var stopButtonElemet = document.getElementById("stopCustomModel") as HTMLButtonElement;
        var playButtonElemet = document.getElementById("playCustomModel") as HTMLButtonElement;
        if(element != undefined) {
          element.innerHTML = status;
        }

        if(startButtonElemet == null || stopButtonElemet == null || playButtonElemet == null){
          return;
        }

        if(status == StatusCustomModel.STOPPED || status == StatusCustomModel.TRAINING_COMPLETED){
          startButtonElemet.disabled = false;
          stopButtonElemet.disabled = true;
          playButtonElemet.disabled = true;
        } else if(status == StatusCustomModel.RUNNING){
          startButtonElemet.disabled = true;
          stopButtonElemet.disabled = false;
          playButtonElemet.disabled = false;
        } else {
          startButtonElemet.disabled = true;
          stopButtonElemet.disabled = true;
          playButtonElemet.disabled = true;
        }
      }
    );
  }

  private getProjectId(){
    var listProjectIdElement = document.getElementById("listProject") as HTMLSelectElement;
    var projectId = "";

    if(listProjectIdElement.options.length > 0){
      projectId = listProjectIdElement.options[listProjectIdElement.selectedIndex].value;
    }

    return projectId;
  }

  private existVersion(projectId:string){
    var result = false;
    if(this.listProject != null){
      for (var i=0; i < this.listProject.length; i++){
        if(this.listProject[i].id == projectId){
          if(this.listProject[i].version_name != null){
            result = true;
            break;
          }
        }
      }
    } else {
      result = false;
    }
    return result;
  }

}
