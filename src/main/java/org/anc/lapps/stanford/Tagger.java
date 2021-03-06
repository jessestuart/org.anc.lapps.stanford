package org.anc.lapps.stanford;

import edu.stanford.nlp.ling.CoreAnnotations.*;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.anc.lapps.serialization.Container;
import org.anc.lapps.serialization.ProcessingStep;
import org.anc.lapps.stanford.util.Converter;
import org.lappsgrid.api.Data;
import org.lappsgrid.core.DataFactory;
import org.lappsgrid.discriminator.Types;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Keith Suderman
 */
public class Tagger extends AbstractStanfordService
{
   private static final Logger logger = LoggerFactory.getLogger(Tagger.class);

   public Tagger()
   {
      super("tokenize, ssplit, pos");
      logger.info("Stanford tagger created.");
   }

   @Override
   public long[] requires()
   {
      return new long[]{Types.TEXT};
   }

   @Override
   public long[] produces()
   {
      return new long[]{Types.STANFORD, Types.TOKEN, Types.POS};
   }

   @Override
   public Data execute(Data input)
   {
      logger.info("Executing Stanford tagger.");
      Container container = createContainer(input);
      if (container == null)
      {
         return input;
      }
      String text = container.getText();
      Annotation document = new Annotation(text);
      Data data = null;
      StanfordCoreNLP service = null;
      try
      {
         service = pool.take();
         service.annotate(document);
         List<CoreLabel> tokens = document.get(TokensAnnotation.class);
         if (tokens == null)
         {
            return DataFactory.error("Stanford tokenizer returned null.");
         }
         ProcessingStep step = Converter.addTokens(new ProcessingStep(), tokens);
         step.getMetadata().put("produced by", "Stanford Tagger");
         container.getSteps().add(step);

         logger.info("Stanford tagger complete.");
         data = DataFactory.json(container.toJson());
      }
      catch (Exception e)
      {
         data = DataFactory.error(e.getMessage());
      }
      finally
      {
         if (service != null)
         {
            pool.add(service);
         }
      }
      return data;
   }
}
