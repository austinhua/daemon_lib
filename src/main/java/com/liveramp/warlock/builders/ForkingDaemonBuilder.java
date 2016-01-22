package com.liveramp.warlock.builders;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.google.common.collect.Maps;
import org.jetbrains.annotations.NotNull;

import com.liveramp.warlock.JobletCallback;
import com.liveramp.warlock.JobletConfig;
import com.liveramp.warlock.JobletConfigProducer;
import com.liveramp.warlock.JobletFactory;
import com.liveramp.warlock.executors.JobletExecutor;
import com.liveramp.warlock.executors.JobletExecutors;
import com.liveramp.warlock.executors.forking.ProcessJobletRunner;

public class ForkingDaemonBuilder<T extends JobletConfig> extends BaseDaemonBuilder<T, ForkingDaemonBuilder<T>> {

  private final String workingDir;
  private final Class<? extends JobletFactory<T>> jobletFactoryClass;
  private int maxProcesses;
  private Map<String, String> envVariables;
  private JobletCallback<T> successCallback;
  private JobletCallback<T> failureCallback;
  private ProcessJobletRunner jobletRunner;

  private static final int DEFAULT_MAX_PROCESSES = 1;
  private static final Map<String, String> DEFAULT_ENV_VARS = Maps.newHashMap();

  public ForkingDaemonBuilder(String workingDir, String identifier, Class<? extends JobletFactory<T>> jobletFactoryClass, JobletConfigProducer<T> configProducer, ProcessJobletRunner jobletRunner) {
    super(identifier, configProducer);
    this.workingDir = workingDir;
    this.jobletFactoryClass = jobletFactoryClass;
    this.jobletRunner = jobletRunner;

    maxProcesses = DEFAULT_MAX_PROCESSES;
    envVariables = DEFAULT_ENV_VARS;
    successCallback = new JobletCallback.None<>();
    failureCallback = new JobletCallback.None<>();
  }

  public ForkingDaemonBuilder<T> setMaxProcesses(int maxProcesses) {
    this.maxProcesses = maxProcesses;
    return this;
  }

  public ForkingDaemonBuilder<T> addToEnvironmentVariables(String envVar, String value) {
    this.envVariables.put(envVar, value);
    return this;
  }

  public ForkingDaemonBuilder<T> setSuccessCallback(JobletCallback<T> callback) {
    this.successCallback = callback;
    return this;
  }

  public ForkingDaemonBuilder<T> setFailureCallback(JobletCallback<T> callback) {
    this.failureCallback = callback;
    return this;
  }

  @NotNull
  @Override
  protected JobletExecutor<T> getExecutor() throws IllegalAccessException, IOException, InstantiationException {
    final String tmpPath = new File(workingDir, identifier).getPath();
    return JobletExecutors.Forked.get(notifier, tmpPath, maxProcesses, jobletFactoryClass, envVariables, successCallback, failureCallback, jobletRunner);
  }
}