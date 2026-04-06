package com.kangraemin.pictalk.data.repository;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import okhttp3.OkHttpClient;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava",
    "cast"
})
public final class ModelRepositoryImpl_Factory implements Factory<ModelRepositoryImpl> {
  private final Provider<Context> contextProvider;

  private final Provider<OkHttpClient> okHttpClientProvider;

  public ModelRepositoryImpl_Factory(Provider<Context> contextProvider,
      Provider<OkHttpClient> okHttpClientProvider) {
    this.contextProvider = contextProvider;
    this.okHttpClientProvider = okHttpClientProvider;
  }

  @Override
  public ModelRepositoryImpl get() {
    return newInstance(contextProvider.get(), okHttpClientProvider.get());
  }

  public static ModelRepositoryImpl_Factory create(Provider<Context> contextProvider,
      Provider<OkHttpClient> okHttpClientProvider) {
    return new ModelRepositoryImpl_Factory(contextProvider, okHttpClientProvider);
  }

  public static ModelRepositoryImpl newInstance(Context context, OkHttpClient okHttpClient) {
    return new ModelRepositoryImpl(context, okHttpClient);
  }
}
