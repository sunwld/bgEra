package com.collie.bgEra.cloudApp.appm

class AppManagerStandardSkillImpl extends AppManagerStandardSkill {
  override def suspend: Unit = {
    println("Skill: execute app suspend")
  }

  override def resume: Unit = {
    println("Skill: execute app resume")
  }

  override def close: Unit = {
    println("Skill: execute app close")
  }

  override def reconstruction: Unit = {
    println("Skill: execute app reconstruction")
  }

  override def reallocation: Unit = {
    println("Skill: execute app reallocation")
  }
}
