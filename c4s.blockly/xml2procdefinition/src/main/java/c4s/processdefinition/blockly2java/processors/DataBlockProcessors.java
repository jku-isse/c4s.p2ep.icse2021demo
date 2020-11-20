package c4s.processdefinition.blockly2java.processors;

import java.util.AbstractMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import c4s.processdefinition.blockly2java.ProcessingContext;
import https.developers_google_com.blockly.xml.BlockType;
import passiveprocessengine.definition.ArtifactType;

public class DataBlockProcessors {

	private static Logger log = LogManager.getLogger(DataBlockProcessors.class);
	
	public static class StreamBlockProcessor extends AbstractBlockProcessor {

		public final static String STREAM_TYPE = "stream";
		
		public StreamBlockProcessor(ProcessingContext ctx) {
			super(STREAM_TYPE, ctx);
		}

		@Override
		public void processBlock(BlockType block) {
			
		}
		
		
		public static Optional<ArtifactType> getTypeFromStream(BlockType block, ProcessingContext ctx) {
			return Utils.getValueByName(block.getStatementOrValue(), "NAME").map(
					value -> getVarFromFieldAccessor(value.getBlock(), ctx).orElse(null));
		}

		public static Optional<ArtifactType> getVarFromFieldAccessor(BlockType block, ProcessingContext ctx) {
			// currently we have no metamodel to look up with field of a type is what other type, so we return just the parent, e.g., the original var
			return Utils.getValueByName(block.getStatementOrValue(), "var").map(
					value -> Utils.getField(value.getBlock().getField(), "VAR").map(
							field -> ctx.getVarIndex().get(field.getId()) ).orElse(null) );
		}
	}
	
	
//	public static class ArtUserProcessor extends AbstractBlockProcessor {
//
//		public final static String TYPE = "artuse";
//		
//		public ArtUserProcessor(ProcessingContext ctx) {
//			super(TYPE, ctx);
//		}
//
//		@Override
//		public void processBlock(BlockType block) {
//			Utils.getFieldValue(block.getField(), "NAME").ifPresent(
//					name ->	Utils.getFirstValue(block.getStatementOrValue()).ifPresent(
//							value -> getArtifactTypeFromBlock(value.getBlock(), ctx).ifPresent(
//									artType -> ctx.getTaskHierarchy().peek().getExpectedOutput().put(name, artType) ) ) );	
//		}
//		
//	}
	
	public static Optional<String> getVariableIdFromBlock(BlockType block) {
		return Utils.getField(block.getField(), "VAR").map(field -> field.getId());
	}
	
	public static Entry<String,String> getVarIdAndNameFromBlock(BlockType block) {
		 return Utils.getField(block.getField(), "VAR").map(field -> new AbstractMap.SimpleEntry<String,String>(field.getId(), field.getValue()) ).orElse(null) ;
	}
	
	public static Optional<ArtifactType> getArtifactTypeFromBlock(BlockType block, ProcessingContext ctx) {		
		switch(block.getType()) {
		case "artifact":
			// create artifact type from type field
			return Utils.getFieldValue(block.getField(), "Type").map(strType -> new ArtifactType(strType));			
		case "fetchartifact":
			// then from artifact below
			return Utils.getValueByName(block.getStatementOrValue(), "Type").map(value -> getArtifactTypeFromBlock(value.getBlock(), ctx).orElseGet(null) ); // orElseGet is needed to unpack inner optional				
		case "variables_get":			
			// if variable then lookup somewhere
			return Utils.getField(block.getField(), "VAR").map(field -> ctx.getVarIndex().get(field.getId()));			
		case "stream":
			// if stream, then try to obtain outputtype of stream
			return DataBlockProcessors.StreamBlockProcessor.getTypeFromStream(block, ctx);
		default:
			log.warn("Unsupported block to extract ArtifactRole/ArtifactInput/Output from, at block: "+block.getId() + "of type: "+block.getType());			
		}	
		return Optional.empty();
	}
}
