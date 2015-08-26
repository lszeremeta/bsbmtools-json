/*
 * Copyright (C) 2015 Łukasz Szeremeta
 *   based on N-Triples serialization implementation
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package benchmark.serializer;

import benchmark.generator.DateGenerator;
import benchmark.generator.Generator;
import benchmark.model.*;
import benchmark.vocabulary.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.Iterator;

public class JSON implements Serializer {
    private FileWriter[] fileWriter;
    private boolean forwardChaining;
    private long nrTriples;
    private int currentWriter = 0;

    public JSON(String file, boolean forwardChaining) {
        this(file, forwardChaining, 1);
    }

    public JSON(String file, boolean forwardChaining, int nrOfOutputFiles) {
        int nrOfDigits = ((int) Math.log10(nrOfOutputFiles)) + 1;
        String formatString = "%0" + nrOfDigits + "d";
        try {
            fileWriter = new FileWriter[nrOfOutputFiles];
            if (nrOfOutputFiles == 1)
                fileWriter[0] = new FileWriter(file + ".json");
            else
                for (int i = 1; i <= nrOfOutputFiles; i++)
                    fileWriter[i - 1] = new FileWriter(file + String.format(formatString, i) + ".json");
        } catch (IOException e) {
            System.err.println("Could not open File");
            System.exit(-1);
        }

        this.forwardChaining = forwardChaining;
        nrTriples = 0l;
    }

    public void gatherData(ObjectBundle bundle) {
        Iterator<BSBMResource> it = bundle.iterator();

        while (it.hasNext()) {
            BSBMResource obj = it.next();
            try {
                if (obj instanceof ProductType) {
                    fileWriter[currentWriter].append(convertProductType((ProductType) obj));
                } else if (obj instanceof Offer) {
                    fileWriter[currentWriter].append(convertOffer((Offer) obj));
                } else if (obj instanceof Product) {
                    fileWriter[currentWriter].append(convertProduct((Product) obj));
                } else if (obj instanceof Person) {
                    fileWriter[currentWriter].append(convertPerson((Person) obj));
                } else if (obj instanceof Producer) {
                    fileWriter[currentWriter].append(convertProducer((Producer) obj));
                } else if (obj instanceof ProductFeature) {
                    fileWriter[currentWriter].append(convertProductFeature((ProductFeature) obj));
                } else if (obj instanceof Vendor) {
                    fileWriter[currentWriter].append(convertVendor((Vendor) obj));
                } else if (obj instanceof Review) {
                    fileWriter[currentWriter].append(convertReview((Review) obj));
                }
            } catch (IOException e) {
                System.err.println("Could not write into File!");
                System.err.println(e.getMessage());
                System.exit(-1);
            }
            currentWriter = (currentWriter + 1) % fileWriter.length;
        }
    }

    /*
     * Converts the ProductType Object into a RDF/JSON
     * representation.
     */
    private String convertProductType(ProductType pType) {
        StringBuffer result = new StringBuffer();
        //First the uriref for the subject
        String subjectURIREF = pType.toStringJSON();

        //rdf:type
        result.append(createTriple(
                subjectURIREF,
                createURIref(RDF.type),
                createURIref(BSBM.ProductType)));

        //rdfs:label
        result.append(createTriple(
                subjectURIREF,
                createURIref(RDFS.label),
                createLiteral(pType.getLabel())));

        //rdfs:comment
        result.append(createTriple(
                subjectURIREF,
                createURIref(RDFS.comment),
                createLiteral(pType.getComment())));

        //rdfs:subClassOf
        if (pType.getParent() != null) {
            String parentURIREF = createURIref(BSBM.INST_NS, "ProductType" + pType.getParent().getNr());
            result.append(createTriple(
                    subjectURIREF,
                    createURIref(RDFS.subClassOf),
                    parentURIREF));
        }

        //dc:publisher
        result.append(createTriple(
                subjectURIREF,
                createURIref(DC.publisher),
                createURIref(BSBM.getStandardizationInstitution(1))));

        //dc:date
        GregorianCalendar date = new GregorianCalendar();
        date.setTimeInMillis(pType.getPublishDate());
        String dateString = DateGenerator.formatDate(date);
        result.append(createTriple(
                subjectURIREF,
                createURIref(DC.date),
                createDataTypeLiteral(dateString, XSD.Date)));

        return result.toString();
    }

    /*
     * Converts the Offer Object into a RDF/JSON
     * representation.
     */
    private String convertOffer(Offer offer) {
        StringBuffer result = new StringBuffer();
        //First the uriref for the subject
        String subjectURIREF = offer.toStringJSON();

        //rdf:type
        result.append(createTriple(
                subjectURIREF,
                createURIref(RDF.type),
                createURIref(BSBM.Offer)));

        //bsbm:product
        int productNr = offer.getProduct();
        int producerNr = Generator.getProducerOfProduct(productNr);
        result.append(createTriple(
                subjectURIREF,
                createURIref(BSBM.product),
                Product.getJSONURIref(productNr, producerNr)));

        //bsbm:vendor
        result.append(createTriple(
                subjectURIREF,
                createURIref(BSBM.vendor),
                Vendor.getJSONURIref(offer.getVendor())));

        //bsbm:price
        result.append(createTriple(
                subjectURIREF,
                createURIref(BSBM.price),
                createDataTypeLiteral(offer.getPriceString(), BSBM.USD)));

        //bsbm:validFrom
        GregorianCalendar validFrom = new GregorianCalendar();
        validFrom.setTimeInMillis(offer.getValidFrom());
        String validFromString = DateGenerator.formatDateTime(validFrom);
        result.append(createTriple(
                subjectURIREF,
                createURIref(BSBM.validFrom),
                createDataTypeLiteral(validFromString, XSD.DateTime)));

        //bsbm:validTo
        GregorianCalendar validTo = new GregorianCalendar();
        validTo.setTimeInMillis(offer.getValidTo());
        String validToString = DateGenerator.formatDateTime(validTo);
        result.append(createTriple(
                subjectURIREF,
                createURIref(BSBM.validTo),
                createDataTypeLiteral(validToString, XSD.DateTime)));

        //bsbm:deliveryDays
        result.append(createTriple(
                subjectURIREF,
                createURIref(BSBM.deliveryDays),
                createDataTypeLiteral(offer.getDeliveryDays().toString(), XSD.Integer)));

        //bsbm:offerWebpage
        result.append(createTriple(
                subjectURIREF,
                createURIref(BSBM.offerWebpage),
                createURIref(offer.getOfferWebpage())));

        //dc:publisher
        result.append(createTriple(
                subjectURIREF,
                createURIref(DC.publisher),
                Vendor.getJSONURIref(offer.getVendor())));

        //dc:date
        GregorianCalendar date = new GregorianCalendar();
        date.setTimeInMillis(offer.getPublishDate());
        String dateString = DateGenerator.formatDate(date);
        result.append(createTriple(
                subjectURIREF,
                createURIref(DC.date),
                createDataTypeLiteral(dateString, XSD.Date)));

        return result.toString();
    }

    /*
     * Converts the Product Object into a RDF/JSON
     * representation.
     */
    private String convertProduct(Product product) {
        StringBuffer result = new StringBuffer();
        //First the uriref for the subject
        String subjectURIREF = product.toStringJSON();

        //rdf:type
        result.append(createTriple(
                subjectURIREF,
                createURIref(RDF.type),
                createURIref(BSBM.Product)));

        //rdfs:label
        result.append(createTriple(
                subjectURIREF,
                createURIref(RDFS.label),
                createLiteral(product.getLabel())));

        //rdfs:comment
        result.append(createTriple(
                subjectURIREF,
                createURIref(RDFS.comment),
                createLiteral(product.getComment())));

        //bsbm:productType
        if (forwardChaining) {
            ProductType pt = product.getProductType();
            while (pt != null) {
                result.append(createTriple(
                        subjectURIREF,
                        createURIref(RDF.type),
                        pt.toStringJSON()));
                pt = pt.getParent();
            }
        } else {
            result.append(createTriple(
                    subjectURIREF,
                    createURIref(RDF.type),
                    product.getProductType().toStringJSON()));
        }

        //bsbm:producer
        result.append(createTriple(
                subjectURIREF,
                createURIref(BSBM.producer),
                Producer.getJSONURIref(product.getProducer())));

        //bsbm:productPropertyNumeric
        Integer[] ppn = product.getProductPropertyNumeric();
        for (int i = 0, j = 1; i < ppn.length; i++, j++) {
            Integer value = ppn[i];
            if (value != null)
                result.append(createTriple(
                        subjectURIREF,
                        createURIref(BSBM.getProductPropertyNumeric(j)),
                        createDataTypeLiteral(value.toString(), XSD.Integer)));
        }

        //bsbm:productPropertyTextual
        String[] ppt = product.getProductPropertyTextual();
        for (int i = 0, j = 1; i < ppt.length; i++, j++) {
            String value = ppt[i];
            if (value != null)
                result.append(createTriple(
                        subjectURIREF,
                        createURIref(BSBM.getProductPropertyTextual(j)),
                        createDataTypeLiteral(value, XSD.String)));
        }

        //bsbm:productFeature
        Iterator<Integer> pf = product.getFeatures().iterator();
        while (pf.hasNext()) {
            Integer value = pf.next();
            result.append(createTriple(
                    subjectURIREF,
                    createURIref(BSBM.productFeature),
                    ProductFeature.getJSONURIref(value)));
        }

        //dc:publisher
        result.append(createTriple(
                subjectURIREF,
                createURIref(DC.publisher),
                Producer.getJSONURIref(product.getProducer())));

        //dc:date
        GregorianCalendar date = new GregorianCalendar();
        date.setTimeInMillis(product.getPublishDate());
        String dateString = DateGenerator.formatDate(date);
        result.append(createTriple(
                subjectURIREF,
                createURIref(DC.date),
                createDataTypeLiteral(dateString, XSD.Date)));

        return result.toString();
    }

    /*
     * Converts the Person Object into a RDF/JSON
     * representation.
     */
    private String convertPerson(Person person) {
        StringBuffer result = new StringBuffer();
        //First the uriref for the subject
        String subjectURIREF = person.toStringJSON();

        //rdf:type
        result.append(createTriple(
                subjectURIREF,
                createURIref(RDF.type),
                createURIref(FOAF.Person)));

        //foaf:name
        result.append(createTriple(
                subjectURIREF,
                createURIref(FOAF.name),
                createLiteral(person.getName())));

        //foaf:mbox_sha1sum
        result.append(createTriple(
                subjectURIREF,
                createURIref(FOAF.mbox_sha1sum),
                createLiteral(person.getMbox_sha1sum())));

        //bsbm:country
        result.append(createTriple(
                subjectURIREF,
                createURIref(BSBM.country),
                createURIref(ISO3166.find(person.getCountryCode()))));

        //dc:publisher
        result.append(createTriple(
                subjectURIREF,
                createURIref(DC.publisher),
                RatingSite.getJSONURIref(person.getPublisher())));

        //dc:date
        GregorianCalendar date = new GregorianCalendar();
        date.setTimeInMillis(person.getPublishDate());
        String dateString = DateGenerator.formatDate(date);
        result.append(createTriple(
                subjectURIREF,
                createURIref(DC.date),
                createDataTypeLiteral(dateString, XSD.Date)));

        return result.toString();
    }

    /*
     * Converts the Producer Object into a RDF/JSON
     * representation.
     */
    private String convertProducer(Producer producer) {
        StringBuffer result = new StringBuffer();
        //First the uriref for the subject
        String subjectURIREF = producer.toStringJSON();

        //rdf:type
        result.append(createTriple(
                subjectURIREF,
                createURIref(RDF.type),
                createURIref(BSBM.Producer)));

        //rdfs:label
        result.append(createTriple(
                subjectURIREF,
                createURIref(RDFS.label),
                createLiteral(producer.getLabel())));

        //rdfs:comment
        result.append(createTriple(
                subjectURIREF,
                createURIref(RDFS.comment),
                createLiteral(producer.getComment())));

        //foaf:homepage
        result.append(createTriple(
                subjectURIREF,
                createURIref(FOAF.homepage),
                createURIref(producer.getHomepage())));

        //bsbm:country
        result.append(createTriple(
                subjectURIREF,
                createURIref(BSBM.country),
                createURIref(ISO3166.find(producer.getCountryCode()))));

        //dc:publisher
        result.append(createTriple(
                subjectURIREF,
                createURIref(DC.publisher),
                producer.toStringJSON()));

        //dc:date
        GregorianCalendar date = new GregorianCalendar();
        date.setTimeInMillis(producer.getPublishDate());
        String dateString = DateGenerator.formatDate(date);
        result.append(createTriple(
                subjectURIREF,
                createURIref(DC.date),
                createDataTypeLiteral(dateString, XSD.Date)));

        return result.toString();
    }

    /*
     * Converts the ProductFeature Object into a RDF/JSON
     * representation.
     */
    private String convertProductFeature(ProductFeature pf) {
        StringBuffer result = new StringBuffer();
        //First the uriref for the subject
        String subjectURIREF = createURIref(BSBM.INST_NS, "ProductFeature" + pf.getNr());

        //rdf:type
        result.append(createTriple(
                subjectURIREF,
                createURIref(RDF.type),
                createURIref(BSBM.ProductFeature)));

        //rdfs:label
        result.append(createTriple(
                subjectURIREF,
                createURIref(RDFS.label),
                createLiteral(pf.getLabel())));

        //rdfs:comment
        result.append(createTriple(
                subjectURIREF,
                createURIref(RDFS.comment),
                createLiteral(pf.getComment())));

        //dc:publisher
        result.append(createTriple(
                subjectURIREF,
                createURIref(DC.publisher),
                createURIref(BSBM.getStandardizationInstitution(pf.getPublisher()))));

        //dc:date
        GregorianCalendar date = new GregorianCalendar();
        date.setTimeInMillis(pf.getPublishDate());
        String dateString = DateGenerator.formatDate(date);
        result.append(createTriple(
                subjectURIREF,
                createURIref(DC.date),
                createDataTypeLiteral(dateString, XSD.Date)));

        return result.toString();
    }

    /*
     * Converts the Vendor Object into a RDF/JSON
     * representation.
     */
    private String convertVendor(Vendor vendor) {
        StringBuffer result = new StringBuffer();
        //First the uriref for the subject
        String subjectURIREF = vendor.toStringJSON();

        //rdf:type
        result.append(createTriple(
                subjectURIREF,
                createURIref(RDF.type),
                createURIref(BSBM.Vendor)));

        //rdfs:label
        result.append(createTriple(
                subjectURIREF,
                createURIref(RDFS.label),
                createLiteral(vendor.getLabel())));

        //rdfs:comment
        result.append(createTriple(
                subjectURIREF,
                createURIref(RDFS.comment),
                createLiteral(vendor.getComment())));

        //foaf:homepage
        result.append(createTriple(
                subjectURIREF,
                createURIref(FOAF.homepage),
                createURIref(vendor.getHomepage())));

        //bsbm:country
        result.append(createTriple(
                subjectURIREF,
                createURIref(BSBM.country),
                createURIref(ISO3166.find(vendor.getCountryCode()))));

        //dc:publisher
        result.append(createTriple(
                subjectURIREF,
                createURIref(DC.publisher),
                vendor.toStringJSON()));

        //dc:date
        GregorianCalendar date = new GregorianCalendar();
        date.setTimeInMillis(vendor.getPublishDate());
        String dateString = DateGenerator.formatDate(date);
        result.append(createTriple(
                subjectURIREF,
                createURIref(DC.date),
                createDataTypeLiteral(dateString, XSD.Date)));

        return result.toString();
    }


    /*
     * Converts the Review Object into a RDF/JSON
     * representation.
     */
    private String convertReview(Review review) {
        StringBuffer result = new StringBuffer();
        //First the uriref for the subject
        String subjectURIREF = review.toStringJSON();

        //rdf:type
        result.append(createTriple(
                subjectURIREF,
                createURIref(RDF.type),
                createURIref(REV.Review)));

        //bsbm:reviewFor
        result.append(createTriple(
                subjectURIREF,
                createURIref(BSBM.reviewFor),
                Product.getJSONURIref(review.getProduct(), review.getProducerOfProduct())));

        //rev:reviewer
        result.append(createTriple(
                subjectURIREF,
                createURIref(REV.reviewer),
                Person.getJSONURIref(review.getPerson(), review.getPublisher())));

        //bsbm:reviewDate
        GregorianCalendar reviewDate = new GregorianCalendar();
        reviewDate.setTimeInMillis(review.getReviewDate());
        String reviewDateString = DateGenerator.formatDateTime(reviewDate);
        result.append(createTriple(
                subjectURIREF,
                createURIref(BSBM.reviewDate),
                createDataTypeLiteral(reviewDateString, XSD.DateTime)));

        //dc:title
        result.append(createTriple(
                subjectURIREF,
                createURIref(DC.title),
                createLiteral(review.getTitle())));

        //rev:text
        result.append(createTriple(
                subjectURIREF,
                createURIref(REV.text),
                createLanguageLiteral(review.getText(), ISO3166.language[review.getLanguage()])));

        //bsbm:ratingX
        Integer[] ratings = review.getRatings();
        for (int i = 0, j = 1; i < ratings.length; i++, j++) {
            Integer value = ratings[i];
            if (value != null)
                result.append(createTriple(
                        subjectURIREF,
                        createURIref(BSBM.getRating(j)),
                        createDataTypeLiteral(value.toString(), XSD.Integer)));
        }

        //dc:publisher
        result.append(createTriple(
                subjectURIREF,
                createURIref(DC.publisher),
                RatingSite.getJSONURIref(review.getPublisher())));

        //dc:date
        GregorianCalendar date = new GregorianCalendar();
        date.setTimeInMillis(review.getPublishDate());
        String dateString = DateGenerator.formatDate(date);
        result.append(createTriple(
                subjectURIREF,
                createURIref(DC.date),
                createDataTypeLiteral(dateString, XSD.Date)));

        return result.toString();
    }


    //Create Literal
    private String createLiteral(String value) {
        StringBuffer result = new StringBuffer();

        result.append("\"type\":\"literal\"");
        result.append(",");
        result.append("\"value\":\"");
        result.append(value);
        result.append("\"");

        return result.toString();
    }

    //Create typed literal
    private String createDataTypeLiteral(String value, String datatypeURI) {
        StringBuffer result = new StringBuffer();
        result.append("\"type\":\"typed-literal\"");
        result.append(",");
        result.append("\"value\":\"");
        result.append(value);
        result.append("\"");
        result.append(",");
        result.append("\"datatype\":\"");
        result.append(datatypeURI);
        result.append("\"");
        return result.toString();
    }

    //Create language tagged literal
    private String createLanguageLiteral(String text, String languageCode) {
        StringBuffer result = new StringBuffer();
        result.append("\"type\":\"literal\"");
        result.append(",");
        result.append("\"value\":\"");
        result.append(text);
        result.append("\"");
        result.append(",");
        result.append("\"lang\":\"");
        result.append(languageCode);
        result.append("\"");
        return result.toString();
    }

    //Creates a triple
    private String createTriple(String subject, String predicate, String object) {
        StringBuffer result = new StringBuffer();
        result.append("{");
        result.append("\"subject\":{");
        result.append(subject);
        result.append("},");

        result.append("\"predicate\":{");
        result.append(predicate);
        result.append("},");

        result.append("\"object\":{");
        result.append(object);
        result.append("}}\n");

        nrTriples++;

        return result.toString();
    }

    //Create URIREF from namespace and element
    private String createURIref(String namespace, String element) {
        StringBuffer result = new StringBuffer();
        result.append("\"type\":\"uri\"");
        result.append(",");
        result.append("\"value\":\"");
        result.append(namespace);
        result.append(element);
        result.append("\"");

        return result.toString();
    }

    //Create URIREF from URI
    private String createURIref(String uri) {
        StringBuffer result = new StringBuffer();
        result.append("\"type\":\"uri\"");
        result.append(",");
        result.append("\"value\":\"");
        result.append(uri);
        result.append("\"");


        return result.toString();
    }

    public void serialize() {
        //Close Files
        try {
            for (int i = 0; i < fileWriter.length; i++) {
                fileWriter[i].flush();
                fileWriter[i].close();
            }
        } catch (IOException e) {
            System.err.println(e.getMessage());
            System.exit(-1);
        }
    }

    public void writeString(String s) {
        try {
            fileWriter[currentWriter].append(s);
        } catch (IOException e) {
            System.err.println("Could not write output.\n" + e.getMessage());
            System.exit(-1);
        }
    }

    public Long triplesGenerated() {
        return nrTriples;
    }
}
