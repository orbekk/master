// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: src/main/java/com/orbekk/stats/experiments.proto

package com.orbekk.stats;

public final class Experiments {
  private Experiments() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  public interface EmptyOrBuilder
      extends com.google.protobuf.MessageOrBuilder {
  }
  public static final class Empty extends
      com.google.protobuf.GeneratedMessage
      implements EmptyOrBuilder {
    // Use Empty.newBuilder() to construct.
    private Empty(Builder builder) {
      super(builder);
    }
    private Empty(boolean noInit) {}
    
    private static final Empty defaultInstance;
    public static Empty getDefaultInstance() {
      return defaultInstance;
    }
    
    public Empty getDefaultInstanceForType() {
      return defaultInstance;
    }
    
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.orbekk.stats.Experiments.internal_static_com_orbekk_stats_Empty_descriptor;
    }
    
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.orbekk.stats.Experiments.internal_static_com_orbekk_stats_Empty_fieldAccessorTable;
    }
    
    private void initFields() {
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized != -1) return isInitialized == 1;
      
      memoizedIsInitialized = 1;
      return true;
    }
    
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      getSerializedSize();
      getUnknownFields().writeTo(output);
    }
    
    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;
    
      size = 0;
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }
    
    private static final long serialVersionUID = 0L;
    @java.lang.Override
    protected java.lang.Object writeReplace()
        throws java.io.ObjectStreamException {
      return super.writeReplace();
    }
    
    public static com.orbekk.stats.Experiments.Empty parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static com.orbekk.stats.Experiments.Empty parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static com.orbekk.stats.Experiments.Empty parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static com.orbekk.stats.Experiments.Empty parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static com.orbekk.stats.Experiments.Empty parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static com.orbekk.stats.Experiments.Empty parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    public static com.orbekk.stats.Experiments.Empty parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      Builder builder = newBuilder();
      if (builder.mergeDelimitedFrom(input)) {
        return builder.buildParsed();
      } else {
        return null;
      }
    }
    public static com.orbekk.stats.Experiments.Empty parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      Builder builder = newBuilder();
      if (builder.mergeDelimitedFrom(input, extensionRegistry)) {
        return builder.buildParsed();
      } else {
        return null;
      }
    }
    public static com.orbekk.stats.Experiments.Empty parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static com.orbekk.stats.Experiments.Empty parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    
    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(com.orbekk.stats.Experiments.Empty prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }
    
    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder>
       implements com.orbekk.stats.Experiments.EmptyOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return com.orbekk.stats.Experiments.internal_static_com_orbekk_stats_Empty_descriptor;
      }
      
      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return com.orbekk.stats.Experiments.internal_static_com_orbekk_stats_Empty_fieldAccessorTable;
      }
      
      // Construct using com.orbekk.stats.Experiments.Empty.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }
      
      private Builder(BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
        }
      }
      private static Builder create() {
        return new Builder();
      }
      
      public Builder clear() {
        super.clear();
        return this;
      }
      
      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }
      
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return com.orbekk.stats.Experiments.Empty.getDescriptor();
      }
      
      public com.orbekk.stats.Experiments.Empty getDefaultInstanceForType() {
        return com.orbekk.stats.Experiments.Empty.getDefaultInstance();
      }
      
      public com.orbekk.stats.Experiments.Empty build() {
        com.orbekk.stats.Experiments.Empty result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }
      
      private com.orbekk.stats.Experiments.Empty buildParsed()
          throws com.google.protobuf.InvalidProtocolBufferException {
        com.orbekk.stats.Experiments.Empty result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(
            result).asInvalidProtocolBufferException();
        }
        return result;
      }
      
      public com.orbekk.stats.Experiments.Empty buildPartial() {
        com.orbekk.stats.Experiments.Empty result = new com.orbekk.stats.Experiments.Empty(this);
        onBuilt();
        return result;
      }
      
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof com.orbekk.stats.Experiments.Empty) {
          return mergeFrom((com.orbekk.stats.Experiments.Empty)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }
      
      public Builder mergeFrom(com.orbekk.stats.Experiments.Empty other) {
        if (other == com.orbekk.stats.Experiments.Empty.getDefaultInstance()) return this;
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }
      
      public final boolean isInitialized() {
        return true;
      }
      
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder(
            this.getUnknownFields());
        while (true) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              this.setUnknownFields(unknownFields.build());
              onChanged();
              return this;
            default: {
              if (!parseUnknownField(input, unknownFields,
                                     extensionRegistry, tag)) {
                this.setUnknownFields(unknownFields.build());
                onChanged();
                return this;
              }
              break;
            }
          }
        }
      }
      
      
      // @@protoc_insertion_point(builder_scope:com.orbekk.stats.Empty)
    }
    
    static {
      defaultInstance = new Empty(true);
      defaultInstance.initFields();
    }
    
    // @@protoc_insertion_point(class_scope:com.orbekk.stats.Empty)
  }
  
  public interface SimpleTimingOrBuilder
      extends com.google.protobuf.MessageOrBuilder {
    
    // optional double timing = 1;
    boolean hasTiming();
    double getTiming();
    
    // optional int32 numDevices = 2;
    boolean hasNumDevices();
    int getNumDevices();
  }
  public static final class SimpleTiming extends
      com.google.protobuf.GeneratedMessage
      implements SimpleTimingOrBuilder {
    // Use SimpleTiming.newBuilder() to construct.
    private SimpleTiming(Builder builder) {
      super(builder);
    }
    private SimpleTiming(boolean noInit) {}
    
    private static final SimpleTiming defaultInstance;
    public static SimpleTiming getDefaultInstance() {
      return defaultInstance;
    }
    
    public SimpleTiming getDefaultInstanceForType() {
      return defaultInstance;
    }
    
    public static final com.google.protobuf.Descriptors.Descriptor
        getDescriptor() {
      return com.orbekk.stats.Experiments.internal_static_com_orbekk_stats_SimpleTiming_descriptor;
    }
    
    protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
        internalGetFieldAccessorTable() {
      return com.orbekk.stats.Experiments.internal_static_com_orbekk_stats_SimpleTiming_fieldAccessorTable;
    }
    
    private int bitField0_;
    // optional double timing = 1;
    public static final int TIMING_FIELD_NUMBER = 1;
    private double timing_;
    public boolean hasTiming() {
      return ((bitField0_ & 0x00000001) == 0x00000001);
    }
    public double getTiming() {
      return timing_;
    }
    
    // optional int32 numDevices = 2;
    public static final int NUMDEVICES_FIELD_NUMBER = 2;
    private int numDevices_;
    public boolean hasNumDevices() {
      return ((bitField0_ & 0x00000002) == 0x00000002);
    }
    public int getNumDevices() {
      return numDevices_;
    }
    
    private void initFields() {
      timing_ = 0D;
      numDevices_ = 0;
    }
    private byte memoizedIsInitialized = -1;
    public final boolean isInitialized() {
      byte isInitialized = memoizedIsInitialized;
      if (isInitialized != -1) return isInitialized == 1;
      
      memoizedIsInitialized = 1;
      return true;
    }
    
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                        throws java.io.IOException {
      getSerializedSize();
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        output.writeDouble(1, timing_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        output.writeInt32(2, numDevices_);
      }
      getUnknownFields().writeTo(output);
    }
    
    private int memoizedSerializedSize = -1;
    public int getSerializedSize() {
      int size = memoizedSerializedSize;
      if (size != -1) return size;
    
      size = 0;
      if (((bitField0_ & 0x00000001) == 0x00000001)) {
        size += com.google.protobuf.CodedOutputStream
          .computeDoubleSize(1, timing_);
      }
      if (((bitField0_ & 0x00000002) == 0x00000002)) {
        size += com.google.protobuf.CodedOutputStream
          .computeInt32Size(2, numDevices_);
      }
      size += getUnknownFields().getSerializedSize();
      memoizedSerializedSize = size;
      return size;
    }
    
    private static final long serialVersionUID = 0L;
    @java.lang.Override
    protected java.lang.Object writeReplace()
        throws java.io.ObjectStreamException {
      return super.writeReplace();
    }
    
    public static com.orbekk.stats.Experiments.SimpleTiming parseFrom(
        com.google.protobuf.ByteString data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static com.orbekk.stats.Experiments.SimpleTiming parseFrom(
        com.google.protobuf.ByteString data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static com.orbekk.stats.Experiments.SimpleTiming parseFrom(byte[] data)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data).buildParsed();
    }
    public static com.orbekk.stats.Experiments.SimpleTiming parseFrom(
        byte[] data,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws com.google.protobuf.InvalidProtocolBufferException {
      return newBuilder().mergeFrom(data, extensionRegistry)
               .buildParsed();
    }
    public static com.orbekk.stats.Experiments.SimpleTiming parseFrom(java.io.InputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static com.orbekk.stats.Experiments.SimpleTiming parseFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    public static com.orbekk.stats.Experiments.SimpleTiming parseDelimitedFrom(java.io.InputStream input)
        throws java.io.IOException {
      Builder builder = newBuilder();
      if (builder.mergeDelimitedFrom(input)) {
        return builder.buildParsed();
      } else {
        return null;
      }
    }
    public static com.orbekk.stats.Experiments.SimpleTiming parseDelimitedFrom(
        java.io.InputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      Builder builder = newBuilder();
      if (builder.mergeDelimitedFrom(input, extensionRegistry)) {
        return builder.buildParsed();
      } else {
        return null;
      }
    }
    public static com.orbekk.stats.Experiments.SimpleTiming parseFrom(
        com.google.protobuf.CodedInputStream input)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input).buildParsed();
    }
    public static com.orbekk.stats.Experiments.SimpleTiming parseFrom(
        com.google.protobuf.CodedInputStream input,
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
        throws java.io.IOException {
      return newBuilder().mergeFrom(input, extensionRegistry)
               .buildParsed();
    }
    
    public static Builder newBuilder() { return Builder.create(); }
    public Builder newBuilderForType() { return newBuilder(); }
    public static Builder newBuilder(com.orbekk.stats.Experiments.SimpleTiming prototype) {
      return newBuilder().mergeFrom(prototype);
    }
    public Builder toBuilder() { return newBuilder(this); }
    
    @java.lang.Override
    protected Builder newBuilderForType(
        com.google.protobuf.GeneratedMessage.BuilderParent parent) {
      Builder builder = new Builder(parent);
      return builder;
    }
    public static final class Builder extends
        com.google.protobuf.GeneratedMessage.Builder<Builder>
       implements com.orbekk.stats.Experiments.SimpleTimingOrBuilder {
      public static final com.google.protobuf.Descriptors.Descriptor
          getDescriptor() {
        return com.orbekk.stats.Experiments.internal_static_com_orbekk_stats_SimpleTiming_descriptor;
      }
      
      protected com.google.protobuf.GeneratedMessage.FieldAccessorTable
          internalGetFieldAccessorTable() {
        return com.orbekk.stats.Experiments.internal_static_com_orbekk_stats_SimpleTiming_fieldAccessorTable;
      }
      
      // Construct using com.orbekk.stats.Experiments.SimpleTiming.newBuilder()
      private Builder() {
        maybeForceBuilderInitialization();
      }
      
      private Builder(BuilderParent parent) {
        super(parent);
        maybeForceBuilderInitialization();
      }
      private void maybeForceBuilderInitialization() {
        if (com.google.protobuf.GeneratedMessage.alwaysUseFieldBuilders) {
        }
      }
      private static Builder create() {
        return new Builder();
      }
      
      public Builder clear() {
        super.clear();
        timing_ = 0D;
        bitField0_ = (bitField0_ & ~0x00000001);
        numDevices_ = 0;
        bitField0_ = (bitField0_ & ~0x00000002);
        return this;
      }
      
      public Builder clone() {
        return create().mergeFrom(buildPartial());
      }
      
      public com.google.protobuf.Descriptors.Descriptor
          getDescriptorForType() {
        return com.orbekk.stats.Experiments.SimpleTiming.getDescriptor();
      }
      
      public com.orbekk.stats.Experiments.SimpleTiming getDefaultInstanceForType() {
        return com.orbekk.stats.Experiments.SimpleTiming.getDefaultInstance();
      }
      
      public com.orbekk.stats.Experiments.SimpleTiming build() {
        com.orbekk.stats.Experiments.SimpleTiming result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(result);
        }
        return result;
      }
      
      private com.orbekk.stats.Experiments.SimpleTiming buildParsed()
          throws com.google.protobuf.InvalidProtocolBufferException {
        com.orbekk.stats.Experiments.SimpleTiming result = buildPartial();
        if (!result.isInitialized()) {
          throw newUninitializedMessageException(
            result).asInvalidProtocolBufferException();
        }
        return result;
      }
      
      public com.orbekk.stats.Experiments.SimpleTiming buildPartial() {
        com.orbekk.stats.Experiments.SimpleTiming result = new com.orbekk.stats.Experiments.SimpleTiming(this);
        int from_bitField0_ = bitField0_;
        int to_bitField0_ = 0;
        if (((from_bitField0_ & 0x00000001) == 0x00000001)) {
          to_bitField0_ |= 0x00000001;
        }
        result.timing_ = timing_;
        if (((from_bitField0_ & 0x00000002) == 0x00000002)) {
          to_bitField0_ |= 0x00000002;
        }
        result.numDevices_ = numDevices_;
        result.bitField0_ = to_bitField0_;
        onBuilt();
        return result;
      }
      
      public Builder mergeFrom(com.google.protobuf.Message other) {
        if (other instanceof com.orbekk.stats.Experiments.SimpleTiming) {
          return mergeFrom((com.orbekk.stats.Experiments.SimpleTiming)other);
        } else {
          super.mergeFrom(other);
          return this;
        }
      }
      
      public Builder mergeFrom(com.orbekk.stats.Experiments.SimpleTiming other) {
        if (other == com.orbekk.stats.Experiments.SimpleTiming.getDefaultInstance()) return this;
        if (other.hasTiming()) {
          setTiming(other.getTiming());
        }
        if (other.hasNumDevices()) {
          setNumDevices(other.getNumDevices());
        }
        this.mergeUnknownFields(other.getUnknownFields());
        return this;
      }
      
      public final boolean isInitialized() {
        return true;
      }
      
      public Builder mergeFrom(
          com.google.protobuf.CodedInputStream input,
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
          throws java.io.IOException {
        com.google.protobuf.UnknownFieldSet.Builder unknownFields =
          com.google.protobuf.UnknownFieldSet.newBuilder(
            this.getUnknownFields());
        while (true) {
          int tag = input.readTag();
          switch (tag) {
            case 0:
              this.setUnknownFields(unknownFields.build());
              onChanged();
              return this;
            default: {
              if (!parseUnknownField(input, unknownFields,
                                     extensionRegistry, tag)) {
                this.setUnknownFields(unknownFields.build());
                onChanged();
                return this;
              }
              break;
            }
            case 9: {
              bitField0_ |= 0x00000001;
              timing_ = input.readDouble();
              break;
            }
            case 16: {
              bitField0_ |= 0x00000002;
              numDevices_ = input.readInt32();
              break;
            }
          }
        }
      }
      
      private int bitField0_;
      
      // optional double timing = 1;
      private double timing_ ;
      public boolean hasTiming() {
        return ((bitField0_ & 0x00000001) == 0x00000001);
      }
      public double getTiming() {
        return timing_;
      }
      public Builder setTiming(double value) {
        bitField0_ |= 0x00000001;
        timing_ = value;
        onChanged();
        return this;
      }
      public Builder clearTiming() {
        bitField0_ = (bitField0_ & ~0x00000001);
        timing_ = 0D;
        onChanged();
        return this;
      }
      
      // optional int32 numDevices = 2;
      private int numDevices_ ;
      public boolean hasNumDevices() {
        return ((bitField0_ & 0x00000002) == 0x00000002);
      }
      public int getNumDevices() {
        return numDevices_;
      }
      public Builder setNumDevices(int value) {
        bitField0_ |= 0x00000002;
        numDevices_ = value;
        onChanged();
        return this;
      }
      public Builder clearNumDevices() {
        bitField0_ = (bitField0_ & ~0x00000002);
        numDevices_ = 0;
        onChanged();
        return this;
      }
      
      // @@protoc_insertion_point(builder_scope:com.orbekk.stats.SimpleTiming)
    }
    
    static {
      defaultInstance = new SimpleTiming(true);
      defaultInstance.initFields();
    }
    
    // @@protoc_insertion_point(class_scope:com.orbekk.stats.SimpleTiming)
  }
  
  public static abstract class Experiment1
      implements com.google.protobuf.Service {
    protected Experiment1() {}
    
    public interface Interface {
      public abstract void registerSample(
          com.google.protobuf.RpcController controller,
          com.orbekk.stats.Experiments.SimpleTiming request,
          com.google.protobuf.RpcCallback<com.orbekk.stats.Experiments.Empty> done);
      
    }
    
    public static com.google.protobuf.Service newReflectiveService(
        final Interface impl) {
      return new Experiment1() {
        @java.lang.Override
        public  void registerSample(
            com.google.protobuf.RpcController controller,
            com.orbekk.stats.Experiments.SimpleTiming request,
            com.google.protobuf.RpcCallback<com.orbekk.stats.Experiments.Empty> done) {
          impl.registerSample(controller, request, done);
        }
        
      };
    }
    
    public static com.google.protobuf.BlockingService
        newReflectiveBlockingService(final BlockingInterface impl) {
      return new com.google.protobuf.BlockingService() {
        public final com.google.protobuf.Descriptors.ServiceDescriptor
            getDescriptorForType() {
          return getDescriptor();
        }
        
        public final com.google.protobuf.Message callBlockingMethod(
            com.google.protobuf.Descriptors.MethodDescriptor method,
            com.google.protobuf.RpcController controller,
            com.google.protobuf.Message request)
            throws com.google.protobuf.ServiceException {
          if (method.getService() != getDescriptor()) {
            throw new java.lang.IllegalArgumentException(
              "Service.callBlockingMethod() given method descriptor for " +
              "wrong service type.");
          }
          switch(method.getIndex()) {
            case 0:
              return impl.registerSample(controller, (com.orbekk.stats.Experiments.SimpleTiming)request);
            default:
              throw new java.lang.AssertionError("Can't get here.");
          }
        }
        
        public final com.google.protobuf.Message
            getRequestPrototype(
            com.google.protobuf.Descriptors.MethodDescriptor method) {
          if (method.getService() != getDescriptor()) {
            throw new java.lang.IllegalArgumentException(
              "Service.getRequestPrototype() given method " +
              "descriptor for wrong service type.");
          }
          switch(method.getIndex()) {
            case 0:
              return com.orbekk.stats.Experiments.SimpleTiming.getDefaultInstance();
            default:
              throw new java.lang.AssertionError("Can't get here.");
          }
        }
        
        public final com.google.protobuf.Message
            getResponsePrototype(
            com.google.protobuf.Descriptors.MethodDescriptor method) {
          if (method.getService() != getDescriptor()) {
            throw new java.lang.IllegalArgumentException(
              "Service.getResponsePrototype() given method " +
              "descriptor for wrong service type.");
          }
          switch(method.getIndex()) {
            case 0:
              return com.orbekk.stats.Experiments.Empty.getDefaultInstance();
            default:
              throw new java.lang.AssertionError("Can't get here.");
          }
        }
        
      };
    }
    
    public abstract void registerSample(
        com.google.protobuf.RpcController controller,
        com.orbekk.stats.Experiments.SimpleTiming request,
        com.google.protobuf.RpcCallback<com.orbekk.stats.Experiments.Empty> done);
    
    public static final
        com.google.protobuf.Descriptors.ServiceDescriptor
        getDescriptor() {
      return com.orbekk.stats.Experiments.getDescriptor().getServices().get(0);
    }
    public final com.google.protobuf.Descriptors.ServiceDescriptor
        getDescriptorForType() {
      return getDescriptor();
    }
    
    public final void callMethod(
        com.google.protobuf.Descriptors.MethodDescriptor method,
        com.google.protobuf.RpcController controller,
        com.google.protobuf.Message request,
        com.google.protobuf.RpcCallback<
          com.google.protobuf.Message> done) {
      if (method.getService() != getDescriptor()) {
        throw new java.lang.IllegalArgumentException(
          "Service.callMethod() given method descriptor for wrong " +
          "service type.");
      }
      switch(method.getIndex()) {
        case 0:
          this.registerSample(controller, (com.orbekk.stats.Experiments.SimpleTiming)request,
            com.google.protobuf.RpcUtil.<com.orbekk.stats.Experiments.Empty>specializeCallback(
              done));
          return;
        default:
          throw new java.lang.AssertionError("Can't get here.");
      }
    }
    
    public final com.google.protobuf.Message
        getRequestPrototype(
        com.google.protobuf.Descriptors.MethodDescriptor method) {
      if (method.getService() != getDescriptor()) {
        throw new java.lang.IllegalArgumentException(
          "Service.getRequestPrototype() given method " +
          "descriptor for wrong service type.");
      }
      switch(method.getIndex()) {
        case 0:
          return com.orbekk.stats.Experiments.SimpleTiming.getDefaultInstance();
        default:
          throw new java.lang.AssertionError("Can't get here.");
      }
    }
    
    public final com.google.protobuf.Message
        getResponsePrototype(
        com.google.protobuf.Descriptors.MethodDescriptor method) {
      if (method.getService() != getDescriptor()) {
        throw new java.lang.IllegalArgumentException(
          "Service.getResponsePrototype() given method " +
          "descriptor for wrong service type.");
      }
      switch(method.getIndex()) {
        case 0:
          return com.orbekk.stats.Experiments.Empty.getDefaultInstance();
        default:
          throw new java.lang.AssertionError("Can't get here.");
      }
    }
    
    public static Stub newStub(
        com.google.protobuf.RpcChannel channel) {
      return new Stub(channel);
    }
    
    public static final class Stub extends com.orbekk.stats.Experiments.Experiment1 implements Interface {
      private Stub(com.google.protobuf.RpcChannel channel) {
        this.channel = channel;
      }
      
      private final com.google.protobuf.RpcChannel channel;
      
      public com.google.protobuf.RpcChannel getChannel() {
        return channel;
      }
      
      public  void registerSample(
          com.google.protobuf.RpcController controller,
          com.orbekk.stats.Experiments.SimpleTiming request,
          com.google.protobuf.RpcCallback<com.orbekk.stats.Experiments.Empty> done) {
        channel.callMethod(
          getDescriptor().getMethods().get(0),
          controller,
          request,
          com.orbekk.stats.Experiments.Empty.getDefaultInstance(),
          com.google.protobuf.RpcUtil.generalizeCallback(
            done,
            com.orbekk.stats.Experiments.Empty.class,
            com.orbekk.stats.Experiments.Empty.getDefaultInstance()));
      }
    }
    
    public static BlockingInterface newBlockingStub(
        com.google.protobuf.BlockingRpcChannel channel) {
      return new BlockingStub(channel);
    }
    
    public interface BlockingInterface {
      public com.orbekk.stats.Experiments.Empty registerSample(
          com.google.protobuf.RpcController controller,
          com.orbekk.stats.Experiments.SimpleTiming request)
          throws com.google.protobuf.ServiceException;
    }
    
    private static final class BlockingStub implements BlockingInterface {
      private BlockingStub(com.google.protobuf.BlockingRpcChannel channel) {
        this.channel = channel;
      }
      
      private final com.google.protobuf.BlockingRpcChannel channel;
      
      public com.orbekk.stats.Experiments.Empty registerSample(
          com.google.protobuf.RpcController controller,
          com.orbekk.stats.Experiments.SimpleTiming request)
          throws com.google.protobuf.ServiceException {
        return (com.orbekk.stats.Experiments.Empty) channel.callBlockingMethod(
          getDescriptor().getMethods().get(0),
          controller,
          request,
          com.orbekk.stats.Experiments.Empty.getDefaultInstance());
      }
      
    }
  }
  
  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_com_orbekk_stats_Empty_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_com_orbekk_stats_Empty_fieldAccessorTable;
  private static com.google.protobuf.Descriptors.Descriptor
    internal_static_com_orbekk_stats_SimpleTiming_descriptor;
  private static
    com.google.protobuf.GeneratedMessage.FieldAccessorTable
      internal_static_com_orbekk_stats_SimpleTiming_fieldAccessorTable;
  
  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n0src/main/java/com/orbekk/stats/experim" +
      "ents.proto\022\020com.orbekk.stats\"\007\n\005Empty\"2\n" +
      "\014SimpleTiming\022\016\n\006timing\030\001 \001(\001\022\022\n\nnumDevi" +
      "ces\030\002 \001(\0052X\n\013Experiment1\022I\n\016RegisterSamp" +
      "le\022\036.com.orbekk.stats.SimpleTiming\032\027.com" +
      ".orbekk.stats.EmptyB\003\210\001\001"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          internal_static_com_orbekk_stats_Empty_descriptor =
            getDescriptor().getMessageTypes().get(0);
          internal_static_com_orbekk_stats_Empty_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_com_orbekk_stats_Empty_descriptor,
              new java.lang.String[] { },
              com.orbekk.stats.Experiments.Empty.class,
              com.orbekk.stats.Experiments.Empty.Builder.class);
          internal_static_com_orbekk_stats_SimpleTiming_descriptor =
            getDescriptor().getMessageTypes().get(1);
          internal_static_com_orbekk_stats_SimpleTiming_fieldAccessorTable = new
            com.google.protobuf.GeneratedMessage.FieldAccessorTable(
              internal_static_com_orbekk_stats_SimpleTiming_descriptor,
              new java.lang.String[] { "Timing", "NumDevices", },
              com.orbekk.stats.Experiments.SimpleTiming.class,
              com.orbekk.stats.Experiments.SimpleTiming.Builder.class);
          return null;
        }
      };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
        }, assigner);
  }
  
  // @@protoc_insertion_point(outer_class_scope)
}